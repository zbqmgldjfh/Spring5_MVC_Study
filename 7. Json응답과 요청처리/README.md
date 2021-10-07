# JSON 응답과 요청 처리 📧
웹 페이지에서 Ajax를 이용해 서버 api를 호출하는 사이트가많다. 이들 api는 웹 요청에 대한 응답으로 HTML대신 JSON이나 XML사용한당.

웹 요청도 쿼리문자열 대신에 json이나 xml을 데이터로 보내기도 한다. GET이나 POST만 사용하지 않고 PUT, DELETE와 같은 다른방식도 자바스크립트를 사용하면 가능하다.

스프링 mvc를 사용하면 이를위한 웹 컨트롤러를 쉽게만들수있당.



## 1. JSON개요 
JSON(JavaScript Object Notation)은 간단한 형식을 갖는 문자열로 데이터 교환에 주로사용한다.

```json
{
    "name:":"유관순"
    "birthday":"1902-12-16",
    "age":17,
    "related":["남동순","류예도"]
    "edu":[
        {
            "title":"이화학당보통과",
            "year":1916
        },
        {
            "title":"이화학당고등과",
            "year":1916
        },
        {
            "title":"이화학당고등과",
            "year":1919
        }
    ]
}
```
JSON규칙은 간단하다. 중괄호를 사용해서 객체를 표현한다. 객체는 (key, value)의 쌍을 갖는다. 
이때 key와 value는 콜론(:)으로 구분한다. 

예를 들어 위의 경우 key가 name인 데이터의 값은 "유관순"이다.

값에는 다음이올수있다.

- 문자열, 숫자, 불리언, null
- 배열
- 다른객체



## 2. Jackson 의존설정

Jackson은 자바 객체와 JSON 형식간에 변환을 처리하는 라이브러리이다. 

스프링 MVC에서 Jackson 라이브러리를 이용해서 자바객체를 JSON으로 변환하려면 클래스패스에 Jackson라이브러리를 추가하면된다.   

pom.xml에 추가해보자.

```xml
<dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.9.4</version>
    </dependency>
    <!-- java8 date/time -->
    <dependency>
        <groupId>com.fasterxml.jackson.datatype</groupId>
        <artifactId>jackson-datatype-jsr310</artifactId>
        <version>2.9.4</version>
    </dependency>
    <!-- java8 Optional, etc -->
    <dependency>
        <groupId>com.fasterxml.jackson.datatype</groupId>
        <artifactId>jackson-datatype-jdk8</artifactId>
        <version>2.9.4</version>
    </dependency>
```
![image](https://user-images.githubusercontent.com/60593969/136330091-2b977937-7de1-427e-aa43-79f0e48b9367.png)

Jackson은 프로퍼티 (get 메소드 또는 설정에 따라 필드)의 이름과 값을 JSON 객체의 (key, value) 쌍으로 사용한다.

위의 그림에서 Person객체의 name과 age의 프로퍼티를 갖는 데이터를 json 형태로 바꾼 모습을 볼수있다.

프로퍼티 타입이 배열이나 List인 경우 JSON배열로 변환된다.



## 3. @RestController로 JSON 형식 응답.
스프링 MVC에서 JSON 형식으로 데이터를 응답하는 것은 간단하당. 

@Controller애노테이션 대신 **@RestController 애노테이션**을 사용하면된다.

```java
@RestController
public class RestMemberController {
    private MemberDao memberDao;
    private MemberRegisterService registerService;
    
    @GetMapping("/api/members")
    public List<Member> members(){
        return memberDao.selectAll();
    }
    
    @GetMapping("/api/members/{id}")
    public Member member(@PathVariable Long id, HttpServletResponse response) throws         IOException{
        Member member = memberDao.selectById(id);
        if(member == null){
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        return member;
    }
    
    public void setMemberDao(MemberDao memberDao) {
        this.memberDao = memberDao;
    }
    
    public void setRegisterService(MemberRegisterService registerService) {
        this.registerService = registerService;
    }
}
```
위 코드에서 기존 컨트롤러 코드와 다른점은 다음과 같다.

- @Controller 애노테이션 대신 @RestController 애노테이션 사용
- 요청 매핑 애노테이션 적용 메소드의 **리턴타입으로 일반 객체 사용**



@RestController 애노테이션을 붙인경우 스프링 MVC는 요청 매핑애노테이션을 붙인 메소드가 리턴한 객체를 알맞은 형식으로 변환해서 응답데이터로 전송한다. 이때 클래스 패스에 Jackson이 존재하면 JSON형식의 문자열로 변환해서 응답한다.

예를 들어 위의 members()메서드는 리턴타입이 List< Member>인데 이경우 해당 List객체를 JSON형식의 배열로 변환해서 응답한다.



이후에 ControllerConfig에 추가해야 한다.

```java
@Bean
public RestMemberController restApi(){
	RestMemberController con = new RestMemberController();
	cont.setMemberDao(memberDao);
	cont.setRegisterService(memberRegSvc);
	return con;
}
```


### 3.1 @JsonIgnore를 이용한 제외 처리
위의 응답결과를 보면 password가 포함되어있다. 보통 암호와 같이 민감한데이터는 응답 결과에 포함시키면 안되므로 password 데이터를 응답 결과에서 제외시켜야한다. 

Jackson이 제공하는 **@JsonIgnore애노테이션**을 사용하면 간단히처리할수있다.

```java
public class Member{
    private Long id;
    private String email;
    @JsonIgnore
    private String password;
    private String name;
    @JsonFormat(shape = Shape.STRING)
    private LocalDateTime registerDateTime;
}
```


### 3.2 응답 데이터의 컨텐츠 형식

개발자도구의 네트워크 탭을보면 아래와같이 응답헤더의 Content-Type가 application/json인 것을 알수있다.

![image](https://user-images.githubusercontent.com/60593969/136331905-c0179da1-9477-4594-b2e5-a8f7bc7189ab.png)





## 4. @RequestBody로 JSON요청처리

응답을 JSON으로 변환하는것에 대해 살펴봤으니 반대로 JSON형식의 요청데이터를 자바객체로 변환하는 기능에 대해 살펴보자.

POST방식이나 PUT방식을 사용하면 name=이름&age=20과 같은 쿼리문자열형식이 아닌 다음과같은 json형식의 데이터를 요청데이터로 전송할수있다.

    {"name":"이름","age":20}
JSON형식으로 전성된 요청데이터를 커맨드 객체로 전달받는 방법은 간단하다. 

커맨드 객체에 @RequestBody에노테이션만 붙이면된다.

![image](https://user-images.githubusercontent.com/60593969/136332461-76cc3da1-c05e-4c23-9c2e-e8c4ebbb5e69.png)

@RequestBody 애노테이션을 커맨드 객체에 붙이면 JSON형식의 문자열을 해당 자바객체로 변환한다.

예를들어 다음과 같은 JSON데이터를 위의 RegisterReqeust타입의 regReq객체로 변환할수있다    

```json
{
    "email":"zbqmgldjfh@naver.com",
    "password":"7777"
    "confirmPassword":"7777"
    "name":"zbqmgldjfh"
}
```

스프링 MVC가 JSON형식으로 전송된 데이터를 올바르게 처리하려면 요청 컨텐츠 타입이 application/json이어야한다. 

보통 POST방식의 폼데이터는 쿼리 문자열인 "p1=v1&p2=v2"로 전송되는데, 컨텐츠 타입은 application/x-www-form-urlencoded이다. 

쿼리 문자열이 아닌 JSON형식을 사용하려면 application/json타입으로 데이터를 전송할 수 있는 별도 프로그램이 필요하다.



### 4.1 JSON데이터의 날짜 형식다루기.
JSON형식의 데이터를 날짜 형식으로 변환하는 방법을 살펴보자. 

별도 설정을 하지않으면 다음 패턴(시간대가 없는 JSR-8601)의 문자열을 LocalDateTime과 Date로 변환한다.

    yyyy-MM-ddTHH:mm:ss

특정 패턴을 가진 문자열을 LocalDateTime이나 Date타임으로 변환하고 싶다면 @JsonFormat 애노테이션의 pattern속성을 사용해서 패턴을 지정한다.

```java
@JsonFormat(pattern="yyyyMMddHHmmss")
private LocalDateTime birthDateTime;

@JsonFormat(pattern="yyyyMMdd HHmmss")
private Date birthDate;
```
특정 속성이 아니라 해당타입을 갖는 모든 속성에 적용하고싶다면 스프링 MVC설정을 추가하면된다.

```java
public class MvcConfig implements WebMvcConfigurer{
    ...생략
        
    @Override
    public void extendMessageConverters(
        List<HttpMessageConverter<?>> converters){
            DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            ObjectMapper objectMapper=Jackson2ObjectMapperBuilder.json()
            .featureToEnable(SerializationFeature.INDENT_OUTPUT)
            .deserializerByType(LocalDateTime.class,new                                               LocalDateTimeDeserializer(formatter))
            .simpleDateFormat("yyyyMMdd HHmmss")
            .build();

            converters.add(0,new MappingJackson2HttpMessageConverter(objectMapper));
        }
} 
```

deserializerByType()는 JSON데이터를 LocalDateTime 타입으로 변환할때 사용할 패턴을 지정하고
simpleDateFormat()은 Date타입으로 변환할때 사용할 패턴을 지정한다.

simpleDateFormat()은 Date타입을 JSON데이터로 변환할때에도 사용된다는점에 유의하자!



### 4.2 요청 객체 검증하기

newMember()메소드를 다시보자 regReq파라미터에 **@Valid애노테이션**이 붙어있다.

```java
@PostMapping("/api/members")
public void newMember(
    @RequestBody @Valid RegisterRequest regReq,
    HttpServletResponse response) throws IOException{
        ...생략
    }
```
JSON형식으로 전송한 데이터를 변환한 객체도 동일한 방식으로 @Valid 애노테이션이나 별도 Validator를 이용해 검증할수있다. 

@Valid애노테이션을 사용한 경우 검즈에 실패하면 400(Bad Request)상태코드를 응답한다.

Validator를 사용할 경우 다음과같이 직접상태코드를 처리해야한다.

```java
@PostMapping("/api/members")
public void newMember(
    @RequestBody RegisterRequest regReq,Errors errors,
    HttpServletResponse response) throws IOException{
        try{
            new RegisterRequestValidator().validate(regReq,errors);
            if(errors.hasErrors()){
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            ...
        }catch(DuplicateMemberException dupEx){
            response.sendError(HttpServletResponse.SC_CONFLICT);
        }
    }
```


## 5. ResponseEntity로 객체 리턴하고 응답코드 지정하기

지금까지 예제코드는 상태코드를 지정하기 위해 HttpServletResponse의 setStatus()메소드와 sendError()메소드를 사용했다.

```java
@GetMapping("/api/members/{id}")
public Member member(@PathVariable Long id,
HttpServletResponse response) throws IOException{
    Member member=memberDao.selectById(id);
    if(member == null){
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        return null;
    }
    return member;
}
```

문제는 위와같이 HttpSevletResponse를 이용해서 404응답을하면 json형식이 아닌 서버가 기본으로 제공하는 HTML을 응답결과로 제공한다는점이다. 

예를들어 위코드는 id에 해당하는Member가 존재하면 해당 객체를 리턴하고 존재하지않으면 404응답을 리턴한다. 즉, 존재할때는 JSON결과를 응답하는데 존재하지않을때는 HTML결과를응답 API를 호출하는 프로그램 입장에서 json응답과 HTML 응답을 모두 처리한느 것은 부담스럽다.

404나 500과 같이 처리에 실패한 경우 HTML 응답데이터 대신에 JSON형식의 응답데이터를
전송해야 API호출 프로그램이 일관된 방법으로 응답을 처리할수있다.



### 5.1 ResponseEntity를 이용한 응답 데이터 처리

정상인경우와 비정상인경우 모두 JSON응답을 전송하는 방법은 ResponseEntity를 사용하는것이다.

먼저 에러 상황일때 응답으로 사용할 ErrorResponse 클래스를 다음과 같이작성하자.

```java
public class ErrorResponse {
    private String message;
    
    public ErrorResponse(String message) {
	    this.message = message;
    }
    
    public String getMessage() {
	    return message;
	}
}   
```
ResponseEntity를 이용하면 member()메소드를 아래와같이 구현할수있다.

```java
@RestController
public class RestMemberController{
    private MemberDao memberDao;
    ...생략
    @GetMapping("/api/members/{id}")
    public ResponseEntity<Object> member(@PathVariable Long id){
        Member member = memberDao.selectById(id);
        if(member == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("no member"));
        }
        return ReponseEntity.status(HttpStatus.OK).body(member);
    }
}     
```
스프링 mvc는 리턴타입이 ResponseEntity이면 ResponseEntity의 body로 지정한 객체를 사용해서 변환을 처리한다.

ResponseEntity를 생성하는 기본방법은 status와 body를 이용해서 상태코드와 JSON으로 변환할 객체를 지정하는것이다.

    ResponseEntity.status(상태코드).body(객체)
상태코드는 HttpStatus열거 타입에 정의된 값을 이용해 정의한다. 

200(OK)응답 코드와 몸체 데이터를 생성할 경우 다음과같이 ok()메소드를 이용해 생성할수도있다.

    ResponseEntity.ok(member)
만약 몸체 내용이없다면 다음과같이 body를 지정하지않고 build()로 바로생성한다.

    ResponseEntity.status(HttpStatus.NOT_FOUND).build()
몸체 내용이 없는 경우 status()메소드 대신에 다음과 같이 관련 메소드를 사용해도된다.

    ResponseEntity.notFound().build()


#### 몸체가 없을때 status()대신 사용할수있는 메소드는 다음과같다.

- noContent():204
- badRequest():400
- notFound():404



newMember()메소드는 다음과같이 201(Created)상태 코드와 Location헤더를 함께전송했다.

    response.setHeader("Location","/api/members/" +newMemberId);
    response.setStatus(HttpServletResponse.SC_CREATED);


같은 코드를 ResponseEntity로 구현하면 다음과 같다. ResponseEntity.created()메소드에 Location 헤더로 전달할 URI를 전달하면된다.

```java
@RestController
public class RestMemberController{
    @PostMapping("/api/members")
    public ResponseEntity<Object> newMember(
        @RequestBody @Valid RegisterRequest regReq){
            try{
                Long newMemberID=registerService.regist(regReq);
                URI uri = URI.create("/api/members/" + newMemberId);
                return ResponseEntity.created(uri).build();
            } catch (DuplicateMemberException dupEx){ 
            	return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        }
} 
```


### 5.2 @ExceptionHandler 적용 메소드에서 ResonseEntity로 응답하기

한 메소드에서 정상응답과 에러응답을 ResponseBody로 생성하면 코드가 중복될수있다.

```java
@GetMapping("/api/members/{id}")
public ResponseEntity<Object> member(@PathVariable Long id){
    Member member=memberDao.selectById(id);
    if(member == null){
        return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse("no member));
    }
    return ResponseEntity.ok(member);
}                        
```
이 코드는 member가 존재하지 않을때 기본 HTML 에러응답대신 JSON응답을 제공하기 위해 ResponseEntity를 사용했다.

그런데 회원이 존재하지않을때 404상태 코드를 응답해야 하는 기능이 많다면 에러 응답을 위해 ResponseEntity를 생성하는 코드가 여러곳에 중복된다.

이럴때 **@ExceptionHandler 애노테이션**을 적용한 메소드에서 에러응답을 처리하도록 구현하면 중복을 없앨수있다

```java
@GetMapping("/api/members/{id}")
public Member member(@PathVariable Long id){
    Member member=memberDao.selectById(id);
    if(member == null){
        throw new MemberNotFoundException();
    }
    return member;
}

@ExceptionHandler(MemberNotFoundException.class)
public ResponseEntity<ErrorResponse> handleNoData(){
    return ResponseEntity
    .status(HttpStatus.NOT_FOUND)
    .body(new ErrorResponse("no member"));
}
```
이코드에서 member()메소드는 Member 자체를 리턴한다. 

회원 데이터가 존재하면 Member 객체를 리턴하므로 JSON으로 변환한 객체를 응답한다. 

회원 데이터가 존재하지않으면 MemberNotFoundException을발생한다. 이 익셉션이 발생하면 @ExceptionHandler 애노테이션을 사용한 handleNoData()메소드가 에러를 처리한다. handleNoData()는 404 상태코드와 ErrorResponse 객체를 몸체로 갖는 ResponseEntity를 리턴한다.

따라서 MemberNotFoundException이 발생하면 상태코드가 404이고 몸체가 JSON 형식인 응답을 전송한다.

@RestControllerAdvice 애노테이션을 이용해서 에러 처리코드를 별도 클래스로 분리할수도있다.
@RestControllAdvice 애노테이션은 @ControllerAdvice 애노테이션과 동일하다. 

차이라면 @RestController 애노테이션과 동일하게 응답을 JSON이나 XML과 같은 형식으로 반환한다는것이다.

```java
//@RestControllerAdvice 사용예시
@RestControllerAdvice("controller")
public class ApiExceptionAdvice{
    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoData(){
        return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse("no member"));
    }
}
```



### 5.3 @Valid 에러 결과를 JSON으로 응답하기

@Valid 애노테이션을 붙인 커맨드 객체가 값 검증에 실패하면 400 상태코드를 읍답한다.

```java
//예시
@PostMapping("api/members")
public ResponseEntity<Object> newMember(
    @RequestBody @Valid RegisterRequest regReq){
        try{
            Long newMemberId = registerService.regist(regReq);
            URI uri=URI.create("/api/members/"+newMemberId);
            return ResponseEntity.created(uri).build();
        }catch(DuplicateMemberException dupEx){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
```
문제는 HttpServletResponse를 이용해서 상태코드를 응답했을때와 마찬가지로 HTML 응답을 전송한다는 점이다.

@Valid 애노테이션을 이용한 검증에 실패했을때 HTML응답데이터 대신에 JSON형식 응답을 제공하고 싶다면 다음과 같이 Errors타입 파라미터를 추가해서 직접에러응답을 생성하면 된다.

```java
@PostMapping("/api/members")
public ResponseEntity<Object> newMember(
    @RequestBody @Valid RegisterRequest regReq,Errors errors){
        if(errors.hasErrors()){
            String errorCodes=errors.getAllErrors() //List<ObjectError>
            .stream()
            .map(error -> error.getCodes()[0]) // error는 ObjectError
            .collect(Collectors.joining(","));

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("errorCodes="+errorCodes));
        }
        ...생략.
    }
```
이 코드는 hasErrors() 메소드를 이용해서 검증에러가 존재하는지확인한다.

검증 에러가 존재하면 getAllErrors()메소드로 모든 에러정보를 구하고 (ErrorObject 타입의 객체목록), 각 에러의 코드 값을 연결한 문자열을 생성해서 errorCodes 변수에 할당한다.

@RequestBody 애노테이션을 붙인경우 @Valid 애노테이션을 붙인 객체의 검증에 실패했을때 Errors타입 파라미터가 존재하지 않으면 MethodArgumentNotValidException이 발생한다. 

따라서 다음과 같이 @ExceptionHandler 애노테이션을 이용해 검증 실패시 에러응답을생성해도된다.

```java
@RestControllerAdvice("controller")
public class ApiExceptionAdvice{
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBindException(
        MethodArgumentNotValidException ex){
            String errorCodes = ex.getBindingResult().getAllErrors()
            .stream()
            .map(error->error.getCodes()[0])
            .collect(Collectors.joining(","));
            return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("errorCodes = " + errorCodes));
        }
}
```

