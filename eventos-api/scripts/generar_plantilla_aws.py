"""Genera una plantilla de AWS con el código de despliegue incluido en UserData."""
import base64
import gzip
import hashlib
import io
import json
import tarfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def main():
    sources = [ROOT / 'pom.xml', ROOT / 'Dockerfile', ROOT / 'compose.yaml']
    sources += sorted(p for p in (ROOT / 'src').rglob('*') if p.is_file())
    buffer = io.BytesIO()
    with tarfile.open(fileobj=buffer, mode='w', format=tarfile.GNU_FORMAT) as archive:
        for path in sources:
            data = path.read_bytes()
            info = tarfile.TarInfo(path.relative_to(ROOT).as_posix())
            info.size = len(data)
            info.mode = 0o644
            info.mtime = 0
            archive.addfile(info, io.BytesIO(data))
    compressed = gzip.compress(buffer.getvalue(), mtime=0)
    digest = hashlib.sha256(compressed).hexdigest()
    encoded = base64.b64encode(compressed).decode()
    script = r'''#!/bin/bash
set -euo pipefail
exec > >(tee -a /var/log/eventos-despliegue.log /dev/console) 2>&1
dnf install -y aws-cfn-bootstrap
finalizar() {
  resultado=$?
  trap - EXIT
  /opt/aws/bin/cfn-signal -e "$resultado" --stack '${AWS::StackName}' --resource ApiInstance --region '${AWS::Region}' || true
  exit "$resultado"
}
trap finalizar EXIT
dnf install -y docker python3
systemctl enable --now docker
mkdir -p /usr/local/lib/docker/cli-plugins
curl --fail --location --retry 3 https://github.com/docker/compose/releases/download/v2.39.4/docker-compose-linux-x86_64 -o /usr/local/lib/docker/cli-plugins/docker-compose
chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
if ! swapon --show | grep -q /swapfile; then
  fallocate -l 2G /swapfile
  chmod 600 /swapfile
  mkswap /swapfile
  swapon /swapfile
  echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi
install -d -m 755 /opt/eventos-api
cd /opt/eventos-api
cat <<'PROYECTO_B64' | base64 --decode > /tmp/eventos-proyecto.tar.gz
___PAYLOAD___
PROYECTO_B64
echo '___DIGEST___  /tmp/eventos-proyecto.tar.gz' | sha256sum --check
tar -xzf /tmp/eventos-proyecto.tar.gz
umask 077
clave=$(python3 -c 'import secrets; print(secrets.token_hex(24))')
printf 'DB_PASSWORD=%s\nAPI_PORT=80\n' "$clave" > .env
unset clave
docker compose up -d --build
for intento in $(seq 1 90); do
  if curl --fail --silent http://127.0.0.1/actuator/health; then
    docker compose ps
    echo 'API de eventos disponible. Despliegue terminado.'
    exit 0
  fi
  sleep 2
done
docker compose logs --tail=80 api
exit 1
'''.replace('___PAYLOAD___', encoded).replace('___DIGEST___', digest)
    if len(script.encode()) > 16384:
        raise RuntimeError('UserData supera el límite de 16 KiB; revisar el tamaño del código')
    ref = lambda name: {'Ref': name}
    attr = lambda name, field: {'Fn::GetAtt': [name, field]}
    resources = {
        'Vpc': {'Type': 'AWS::EC2::VPC', 'Properties': {
            'CidrBlock': '10.86.0.0/16', 'EnableDnsSupport': True,
            'EnableDnsHostnames': True, 'Tags': [{'Key': 'Name', 'Value': 'eventos-vpc'}]}},
        'InternetGateway': {'Type': 'AWS::EC2::InternetGateway'},
        'GatewayAttachment': {'Type': 'AWS::EC2::VPCGatewayAttachment', 'Properties': {
            'VpcId': ref('Vpc'), 'InternetGatewayId': ref('InternetGateway')}},
        'PublicSubnet': {'Type': 'AWS::EC2::Subnet', 'Properties': {
            'VpcId': ref('Vpc'), 'CidrBlock': '10.86.1.0/24', 'MapPublicIpOnLaunch': True,
            'AvailabilityZone': {'Fn::Select': [0, {'Fn::GetAZs': ''}]}}},
        'RouteTable': {'Type': 'AWS::EC2::RouteTable', 'Properties': {'VpcId': ref('Vpc')}},
        'PublicRoute': {'Type': 'AWS::EC2::Route', 'DependsOn': 'GatewayAttachment',
            'Properties': {'RouteTableId': ref('RouteTable'),
                'DestinationCidrBlock': '0.0.0.0/0', 'GatewayId': ref('InternetGateway')}},
        'RouteAssociation': {'Type': 'AWS::EC2::SubnetRouteTableAssociation', 'Properties': {
            'SubnetId': ref('PublicSubnet'), 'RouteTableId': ref('RouteTable')}},
        'ApiSecurityGroup': {'Type': 'AWS::EC2::SecurityGroup', 'Properties': {
            'GroupDescription': 'HTTP publico para API de eventos; PostgreSQL queda interno',
            'VpcId': ref('Vpc'), 'SecurityGroupIngress': [{'IpProtocol': 'tcp',
                'FromPort': 80, 'ToPort': 80, 'CidrIp': '0.0.0.0/0',
                'Description': 'API REST del parcial'}]}},
        'ApiInstance': {'Type': 'AWS::EC2::Instance',
            'DependsOn': ['PublicRoute', 'RouteAssociation'],
            'CreationPolicy': {'ResourceSignal': {'Count': 1, 'Timeout': 'PT30M'}},
            'Properties': {
                'ImageId': ref('AmazonLinuxImage'), 'InstanceType': ref('InstanceType'),
                'CreditSpecification': {'CPUCredits': 'standard'},
                'MetadataOptions': {'HttpEndpoint': 'enabled', 'HttpTokens': 'required'},
                'NetworkInterfaces': [{'DeviceIndex': '0', 'AssociatePublicIpAddress': True,
                    'DeleteOnTermination': True, 'GroupSet': [ref('ApiSecurityGroup')],
                    'SubnetId': ref('PublicSubnet')}],
                'BlockDeviceMappings': [{'DeviceName': '/dev/xvda', 'Ebs': {
                    'VolumeType': 'gp3', 'VolumeSize': 16, 'Encrypted': True,
                    'DeleteOnTermination': True}}],
                'Tags': [{'Key': 'Name', 'Value': 'eventos-api-parcial'}],
                'UserData': {'Fn::Base64': {'Fn::Sub': script}}}}
    }
    api_url = {'Fn::Join': ['', ['http://', attr('ApiInstance', 'PublicDnsName')]]}
    template = {'AWSTemplateFormatVersion': '2010-09-09',
        'Description': 'API REST Eventos: Spring Boot, Docker, PostgreSQL y EC2. Crea recursos facturables; revisar el plan de la cuenta.',
        'Metadata': {'Application': 'eventos-api', 'PayloadSHA256': digest},
        'Parameters': {
            'AmazonLinuxImage': {'Type': 'AWS::SSM::Parameter::Value<AWS::EC2::Image::Id>',
                'Default': '/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-x86_64'},
            'InstanceType': {'Type': 'String', 'Default': 't3.micro',
                'AllowedValues': ['t3.micro', 't3.small'],
                'Description': 't3.micro como opcion inicial; t3.small dispone de mas memoria y tiene mayor coste.'}},
        'Resources': resources,
        'Outputs': {
            'ApiUrl': {'Description': 'URL publica para la entrega', 'Value': api_url},
            'HealthUrl': {'Description': 'Comprobacion de salud', 'Value': {'Fn::Join': ['', [api_url, '/actuator/health']]}},
            'EventosUrl': {'Description': 'Listado de eventos', 'Value': {'Fn::Join': ['', [api_url, '/api/eventos']]}},
            'InstanceId': {'Description': 'Instancia EC2 que ejecuta los contenedores', 'Value': ref('ApiInstance')}}}
    target = ROOT / 'aws' / 'eventos-stack.json'
    target.parent.mkdir(exist_ok=True)
    target.write_text(json.dumps(template, ensure_ascii=False, indent=2), encoding='utf-8')
    print(f'Plantilla generada: {target.name}; UserData: {len(script.encode())}/16384 bytes; archivos: {len(sources)}')


if __name__ == '__main__':
    main()
