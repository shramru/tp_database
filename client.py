import json
import urllib2

data = {
        'username': 'vlad',
        'about': 'vladislav',
        'name': 'vladislav',
        'email': 'vladislav@mail.ru',
        'isAnonymous' : False
}

req = urllib2.Request('http://localhost:9998/db/api/user/create/')
req.add_header('Content-Type', 'application/json')

print json.dumps(data)

response = urllib2.urlopen(req, json.dumps(data))

response_body = response.read()
print response_body