import urllib.request
import json

users = [
    ("estudiante@uce.edu.ec", "password123"),
    ("tutor@uce.edu.ec", "password123"),
    ("coordinador@uce.edu.ec", "password123")
]

for email, password in users:
    url = 'http://54.227.79.26:8082/api/auth/login'
    body = json.dumps({'email': email, 'password': password}).encode()
    req = urllib.request.Request(url, data=body, headers={'Content-Type': 'application/json'}, method='POST')
    try:
        resp = urllib.request.urlopen(req)
        print(f'{email} -> OK (status: {resp.status})')
    except urllib.error.HTTPError as e:
        print(f'{email} -> ERROR (status: {e.code}) | {e.read().decode()}')
    except Exception as e:
        print(f'{email} -> ERROR | {e}')
