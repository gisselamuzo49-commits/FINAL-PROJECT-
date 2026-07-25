import subprocess
import os

script_content = '''#!/bin/bash
docker network create pasantias-net 2>/dev/null || true
sudo mkdir -p /var/log/pasantias
sudo chmod 777 /var/log/pasantias
JWT_SECRET='v9y$B&E)H@McQfTjWmZq4t7w!z%C*F-JaNdRgUkXp2r5u8x/A?D(G+KbPeShVmYq'

docker rm -f auth-service internship-service user-service linkage-service gateway-service frontend-web 2>/dev/null || true

docker run -d --name auth-service --network pasantias-net --restart always -p 8080:8080 -v /var/log/pasantias:/var/log/pasantias -e DB_HOST=postgres-db -e DB_PORT=5432 -e DB_NAME=auth_db -e DB_USER=postgres -e DB_PASSWORD=postgres -e JWT_SECRET="$JWT_SECRET" gdmuzo/auth-service:qa
docker run -d --name internship-service --network pasantias-net --restart always -p 8081:8081 -v /var/log/pasantias:/var/log/pasantias -e DB_HOST=postgres-db -e DB_PORT=5432 -e DB_NAME=internship_db -e DB_USER=postgres -e DB_PASSWORD=postgres -e JWT_SECRET="$JWT_SECRET" gdmuzo/internship-service:qa
docker run -d --name user-service --network pasantias-net --restart always -p 8083:8083 -v /var/log/pasantias:/var/log/pasantias -e DB_HOST=postgres-db -e DB_PORT=5432 -e DB_NAME=user_db -e DB_USER=postgres -e DB_PASSWORD=postgres -e JWT_SECRET="$JWT_SECRET" gdmuzo/user-service:qa
docker run -d --name linkage-service --network pasantias-net --restart always -p 8084:8084 -v /var/log/pasantias:/var/log/pasantias -e DB_HOST=postgres-db -e DB_PORT=5432 -e DB_NAME=linkage_db -e DB_USER=postgres -e DB_PASSWORD=postgres -e JWT_SECRET="$JWT_SECRET" gdmuzo/linkage-service:qa

docker run -d --name gateway-service --network pasantias-net --restart always -p 8082:8082 -v /var/log/pasantias:/var/log/pasantias -e AUTH_SERVICE_URL=http://auth-service:8080 -e INTERNSHIP_SERVICE_URL=http://internship-service:8081 -e USER_SERVICE_URL=http://user-service:8083 -e LINKAGE_SERVICE_URL=http://linkage-service:8084 -e REDIS_HOST=redis -e REDIS_PORT=6379 -e JWT_SECRET="$JWT_SECRET" gdmuzo/gateway-service:qa

docker run -d --name frontend-web --network pasantias-net --restart always -p 80:80 gdmuzo/frontend-web:qa
'''

with open('/tmp/start.sh', 'w') as f:
    f.write(script_content)

os.system("scp -i /home/ubuntu/.ssh/qa_key.pem -o StrictHostKeyChecking=no /tmp/start.sh ubuntu@10.0.1.251:/tmp/start.sh")
os.system("ssh -i /home/ubuntu/.ssh/qa_key.pem -o StrictHostKeyChecking=no ubuntu@10.0.1.251 'chmod +x /tmp/start.sh && /tmp/start.sh'")
