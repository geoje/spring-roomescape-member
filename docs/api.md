## API 명세

### 목록

- 예약
    - [예약 조회](#예약-조회)
    - [예약 추가](#예약-추가)
    - [예약 취소](#예약-취소)
- 시간
    - [시간 조회](#시간-조회)
    - [시간 추가](#시간-추가)
    - [시간 삭제](#시간-삭제)
- 테마
    - [테마 조회](#테마-조회)
    - [테마 추가](#테마-추가)
    - [테마 삭제](#테마-삭제)
    - [인기 테마](#인기-테마)
- 인증
    - [로그인](#로그인)
    - [인증 정보 조회](#인증-정보-조회)

<br>

### 예약 조회

**Request**

```http request
GET /reservations HTTP/1.1
```

<br>

**Response**

```http request
HTTP/1.1 200
Content-Type: application/json

[
    {
        "id": 1,
        "name": "브라운",
        "date": "2023-01-01",
        "time": {
            "id": 1,
            "startAt": "10:00"
        }
    },
    {
        "id": 2,
        "name": "브라운",
        "date": "2023-01-02",
        "time": {
            "id": 1,
            "startAt": "10:00"
        }
    }
]
```

<br>

### 예약 추가

**Request**

```http request
POST /reservations HTTP/1.1
content-type: application/json

{
    "date": "2023-08-05",
    "name": "브라운",
    "timeId": 1
}
```

<br>

**Response**

```http request
HTTP/1.1 201
Location: reservations/{id}
Content-Type: application/json

{
    "id": 1,
    "name": "브라운",
    "date": "2023-08-05",
    "time": {
        "id": 1,
        "startAt": "10:00"
    },
    "theme": {
        "id": 1,
        "name": "레벨2 탈출",
        "description": "우테코 레벨2를 탈출하는 내용입니다.",
        "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    }
}

```

<br>

### 예약 취소

**Request**

```http request
DELETE /reservations/1 HTTP/1.1
```

**Response**

```http request
HTTP/1.1 204
```

<br>

### 시간 조회

**Request**

```http request
GET /times HTTP/1.1
```

**Response**

```http request
HTTP/1.1 200
Content-Type: application/json

[
   {
        "id": 1,
        "startAt": "10:00",
        "booked": false
   }
]
```

<br>

### 시간 추가

**Request**

```http request
POST /times HTTP/1.1
content-type: application/json

{
    "startAt": "10:00"
}
```

**Response**

```http request
HTTP/1.1 201
Location: /times/{id}
Content-Type: application/json

{
    "id": 1,
    "startAt": "10:00"
}
```

<br>

### 시간 삭제

**Request**

```http request
DELETE /times/1 HTTP/1.1
```

**Response**

```http request
HTTP/1.1 204
```

<br>

### 테마 조회

**request**

```http request
GET /themes HTTP/1.1
```

**response**

```http request
HTTP/1.1 200
Content-Type: application/json

[
   {
        "id": 1,
        "name": "레벨2 탈출",
        "description": "우테코 레벨2를 탈출하는 내용입니다.",
        "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
   }
]
```

<br>

### 테마 추가

**request**

```http request
POST /themes HTTP/1.1
content-type: application/json

{
    "name": "레벨2 탈출",
    "description": "우테코 레벨2를 탈출하는 내용입니다.",
    "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
}

```

**response**

```http request
HTTP/1.1 201
Location: /themes/1
Content-Type: application/json

{
    "id": 1,
    "name": "레벨2 탈출",
    "description": "우테코 레벨2를 탈출하는 내용입니다.",
    "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
}
```

<br>

### 테마 삭제

**request**

```http request
DELETE /themes/1 HTTP/1.1
```

**response**

```http request
HTTP/1.1 204
```

<br>

### 인기 테마

**request**

```http request
GET /themes/popular HTTP/1.1
```

**response**

```http request
HTTP/1.1 200
Content-Type: application/json

[
    {
        "name": "theme1",
        "thumbnail": "https://abc.com/thumb.png",
        "description": "spring desc"
    }
]
```

<br>

### 로그인

**request**

```http request
POST /login HTTP/1.1
content-type: application/json
host: localhost:8080

{
    "password": "password",
    "email": "admin@email.com"
}
```

**response**

```http request
HTTP/1.1 200 OK
Content-Type: application/json
Keep-Alive: timeout=60
Set-Cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI; Path=/; HttpOnly
```

<br>

### 인증 정보 조회

**request**

```http request
GET /login/check HTTP/1.1
cookie: _ga=GA1.1.48222725.1666268105; _ga_QD3BVX7MKT=GS1.1.1687746261.15.1.1687747186.0.0.0; Idea-25a74f9c=3cbc3411-daca-48c1-8201-51bdcdd93164; token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
host: localhost:8080
```

```http request
HTTP/1.1 200 OK
Connection: keep-alive
Content-Type: application/json
Date: Sun, 03 Mar 2024 19:16:56 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

{
    "name": "어드민"
}
```
