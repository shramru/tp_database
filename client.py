import json, urllib2, time

data = {
        'username': 'vlad',
        'about': 'vladislav',
        'name': 'vladislav',
        'email': 'vladislav@mail.ru',
        'isAnonymous' : False
}

req = urllib2.Request('http://localhost:9998/db/api/user/create/')
req.add_header('Content-Type', 'application/json')
start = time.time()
response = urllib2.urlopen(req, json.dumps(data))
end = time.time()

print json.dumps(data)
print (end - start) * 1000
print response.read()