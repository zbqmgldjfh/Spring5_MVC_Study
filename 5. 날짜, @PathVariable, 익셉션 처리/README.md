# MVC4: 날짜 값 변환, @PathVariable, 익셉션처리 🔧


## 1. 날짜를 이용한 회원 검색기능

MemberDao클래스에 날짜로 유저를 선택할수 있는 selectByRegdate() 메소드를 추가하자!

```java
public List<Member> selectByRegdate(LocalDateTime from, LocalDateTime to) {
	List<Member> results = jdbcTemplate.query(
			"select * from MEMBER where REGDATE between ? and ? " +
			"order by REGDATE desc", new RowMapper<Member>() {
				@Override
				public Member mapRow(ResultSet rs, int rownum) throws SQLException {
					Member member = new Member(
							rs.getString("EMAIL"),
							rs.getString("PASSWORD"),
							rs.getString("NAME"),
							rs.getTimestamp("REGDATE").toLocalDateTime());
					member.setId(rs.getLong("ID"));
					return member;
				}
			},
			from, to);
	return results;
}
```

selectByRegdate()메소드는 REGDATE값이 두 파라미터로 전달받은 from과 to사이에 있는 Member목록을
구한다. 이메소드를 이용해서 특정기간 동안 가입한 회원목록을 보여줄 예정이다.



## 2.커맨드 객체 Date타입 프로퍼티 변환 처리: @DateTimeFormat

회원이 가입한 날짜를 기준으로 회원을 검색하기 위해 시작 시간 기준과, 끝 시간 기준을 인자로 넘겨받는다고 하자. 검색 기준시간을 표현하기위해 아래의 <u>커맨드 클래스</u>를 구현해 사용한다. 

```java
public class ListCommand {

    @DateTimeFormat(pattern = "yyyyMMddHH")
    private LocalDateTime from;
    @DateTimeFormat(pattern = "yyyyMMddHH")
    private LocalDateTime to;

    public LocalDateTime getFrom() {
        return from;
    }

    public void setFrom(LocalDateTime from) {
        this.from = from;
    }

    public LocalDateTime getTo() {
        return to;
    }

    public void setTo(LocalDateTime to) {
        this.to = to;
    }
}
```
유저가 처음 보는 페이지의 Form에서 < input>에 입력한 문자열을 LocalDateTime 타입으로 변환해야 한다.

위처럼 커맨드 객체에 @DateTimeFormat 애노테이션을 적용하였다.

@DateTimeFormat이 적용되면, 지정한 형식을 이용해서 문자열을 LocalDateTime 타입으로 변환한다. 예를들어 위에서는 형식을 "yyyyMMddHH"로 주었는데, 이 경우 "2018030115"를 "2018년 3월 1일 15시" 값을 갖는 LocalDateTime 객체로 변환해준다.



**컨트롤러 클래스는 아래와같이 별도설정없이 ListCommand클래스를 커맨드객체로 사용하면 된다.**

책에서와 달리 생성자 주입 방식을 이용하였다.

```java
@Controller
public class MemberListController {

	private MemberDao memberDao;
	
	public MemberListController(MemberDao memberDao) {
		this.memberDao = memberDao;
	}
	
	@RequestMapping("/members")
	public String list(@ModelAttribute("cmd")ListCommand listCommand, Model model) {
		if(listCommand.getFrom() != null && listCommand.getTo() != null) {
			List<Member> members = memberDao.selectByRegdate(listCommand.getFrom(),                   listCommand.getTo());
			model.addAttribute("members", members);
		}
		return "member/memberList";
	}
} 
```
새로운 컨트롤러 코드를 작성했으니 ControllerConfig설정클래스에 빈을추가하자.

```java
@Autowired
private MemberDao memberDao;

@Bean
public MemberListController memberListController(){
	return new MemberListController(memberDao);
}
```

jsp는 코드생략하였습니다. src/main/webapp/WEB-INF/view/member/폴더를 확인하면 됩니다.

http://localhost:8080/sp5-chap14/members/list 을 직접 입력하면 from파라미터와 to파라미터가 전달되지 못하게 되고, 따라서 커맨드 객체의 from프로퍼티와 to프로퍼티값은 null이 된다. 위의 코드 부분에서 null이 아닐 경우에만 Member 데이터를 읽어오도록 구현하였다.

대신 http://localhost:8080/sp5-chap14/members로 들어와 from과 to에 시간을입력하고 조회를 누르면된다!

 ![image](https://user-images.githubusercontent.com/60593969/135700309-c99ebfd7-7014-408f-83a8-e4f7fa2310e9.png)

결과를 보면 폼에 입력한 2021010112 값이 LoginCommand 객체의 LocalDateTime 프로퍼티인 from으로 알맞게 변환된것을 알수있다.



### 2.1 변환 에러 처리

위 뷰에서 from이나 to에 20190712를 입력하면 지정한형식은 "yyyyMMddHH"이기 때문에 "yyyMMdd"부분만 조회하면 지정한 형식과 일치하지않아 400 에러를 보여준다. (4xx 에러는 클라이언트의 잘못된 요청이라 알려준다.)

400 에러대신 알맞은 에러메시지를 보여주고싶다면 MemberListController에 다음과같이추가하자.

기존코드에 Errors를 추가한것일 뿐이다.

```java
@Controller
public class MemberListController {

	private MemberDao memberDao;
	
	public MemberListController(MemberDao memberDao) {
		this.memberDao = memberDao;
	}
	
	@RequestMapping("/members")
	public String list(@ModelAttribute("cmd")ListCommand listCommand, Errors errors,             Model model) {
		if(errors.hasErrors()) {
			return "member/memberList";
		}
		if(listCommand.getFrom() != null && listCommand.getTo() != null) {
			List<Member> members = memberDao.selectByRegdate(listCommand.getFrom(),                   listCommand.getTo());
			model.addAttribute("members", members);
		}
		return "member/memberList";
	}
} 
```

Errors 타입의 파라미터를 갖을경우 @DateTimeFormat에 지정한 형식이 맞지 않으면 Errors 객체에 "typeMismatch" 에러 코드를 추가한다. 따라서 hasErrors() 메서드로 에러 발생유무를 확인하여 알맞은 처리를 하면 된다.



## 3. 변환 처리에 대한 이해

**궁금증이 있다. 누가 문자열을 LocalDateTime타입으로 변환하는 것 일까?**

답은 **WebDataBinder**에 있다. 

스프링 MVC는 요청 매핑 애노테이션 적용 메소드와 DispatcherSevlet사이를 연결하기 위해 **RequestMappingHandlerAdapte객체**를 사용한다.

이 핸들러 어댑터 객체는 요청 파라미터와 커맨드 객체사이의 변환처리를 위해 **WebDataBinder**를 이용한다.

WebDataBinder는 커맨드객체를 생성한다. 그리고 커맨드 객체의 프로퍼티와 같은 이름을 갖는 요청파라미터를 이용해 프로퍼티값을생성한다.

![image](https://user-images.githubusercontent.com/60593969/135700752-9502e173-ab64-4cb4-9b92-3d8f339f0f0c.png)

WebDataBinder는 직접 타입을 변환하지 않고 위 그림처럼 ConversionService에 그 역할을 **위임**한다.

스프링 MVC를 위한 설정인 @EnableWebMvc애노테이션을 사용하면 DefaultFormattingConversionService을 ConversionSevice로 사용한다.

DefaultFormattinConversionService는 int, long과 같은 기본 데이터 타입뿐만 아니라 @DateTimeFormat 애노테이션을 사용한 시간관련 타입 변환기능을 제공한다. 이런이유로 커맨드로 사용할 클래스에 @DateTimeFormat 애노테이션만 붙이면 지정한 형식의 문자열을 시간타입 값으로받을수있는것이다.

---
WebDataBinder는 < form:input>에도 사용된다. < form:input>태그를 사용하면 아래와 같이 path속성에 지정한 String값인 "from" 으로 id 와 name의 값을 지정하고, "from"의 프로퍼티 값을 String으로 변환하여 value 속성값으로 "2018030115"을 생성한다 .

이때 프로퍼티값을 String으로 변환할때 WebDataBinder의 ConversionService를 사용한다.

![image](https://user-images.githubusercontent.com/60593969/135700943-5dd14804-caab-479f-b7bd-4231df89a056.png)



## 4. @PathVariable을 이용한 경로 변수처리
다음은 id가 1인 회원의 정보를 조회하기 위한 URL이다.

    http://localhost:8080/sp5-chap14/members/1  

이 형식의 url을 사용하면 각 회원마다 경로의 마지막 부분이 달라진다. 이렇게 경로의 일부가 고정되지않고 달라질때 사용할수 있는것이 **@PathVariable 애노테이션**이다.

@PathVariable애노테이션을 사용하면 아래와같이 가변경로를 처리할 수 있다.  


```java
@Controller
public class MemberDetailController {

	private MemberDao memberDao;
	
	@Autowired
	public MemberDetailController(MemberDao memberDao){
		this.memberDao = memberDao;
	}
	
	@GetMapping("/members/{id}")
	public String detail(@PathVariable("id") Long memId, Model model) {
		Member member = memberDao.selectById(memId);
		if(member == null) {
			throw new MemberNotFoundException();
		}
		model.addAttribute("member", member);
		return "member/memberDetail";
	}
}

```
매핑경로에 '{경로변수}'와 같이 중괄호로 둘러 쌓인 부분을 경로변수라고 부른다. "{경로변수}"에 해당하는 값은

같은 경로변수 이름을 지정한 @PathVariable 파라미터에 전달된다.

이제 ControllerConfig에 설정을 추가해보자.

```java
@Bean
public MemberDetailController memberDetailController(){
	return MemberDetailController(memberDao);
}
```

이제 http://localhost:8080/sp5-chap14/members/3 과 같이 입력하면, 아이디가 3인 회원이 존재하면 @PathVariable을 통해 전달받은 경로 변수값을 사용하여 회원정보를 읽어와 뷰에 전달한다.

![image](https://user-images.githubusercontent.com/60593969/135701333-2de13964-d0f3-4397-8260-41609df53ba7.png)



## 6. 컨트롤러 익셉션 처리하기

없는 ID를 경로변수로 사용한다면 MemberNotFoundException이 발생한다. 또한 MemberDetailController가 사용하는 경로변수는 Long타입인데, 실제 요청경로에 숫자가 아닌 문자를 입력할 경우 Long타입으로 변환할수없기에 **400에러가 발생한다**.



**타입변환 실패에 따른 익셉션은 어떻게해야 에러화면을 보여줄수있을까?**



#### 이럴때 사용하는것이 바로 @ExceptionHandler 애노테이션이다!



같은 컨트롤러에 @ExceptionHandler 애노테이션을 적용한 메소드가 존재하면 그 메소드가 익셉션을 처리한다. 따라서 컨트롤러에서 발생한 익셉션을 직접 처리하고싶다면 @ExceptionHandler 애노테이션을 적용한 메소드를 구현하면된다. 다음 코드를 살펴보자.

```java
@Controller
public class MemberDetailController {

	private MemberDao memberDao;
	
	@Autowired
	public MemberDetailController(MemberDao memberDao){
		this.memberDao = memberDao;
	}
	
	@GetMapping("/members/{id}")
	public String detail(@PathVariable("id") Long memId, Model model) {
		Member member = memberDao.selectById(memId);
		if(member == null) {
			throw new MemberNotFoundException();
		}
		model.addAttribute("member", member);
		return "member/memberDetail";
	}
	
	@ExceptionHandler(TypeMismatchException.class)
	public String handleTypeMismatchException() {
		return "member/invalidId";
	}
	
	@ExceptionHandler(MemberNotFoundException.class)
	public String handleNotFoundException() {
		return "member/noMember";
	}
}
```
위 코드를 보면 @ExceptionHandler의 인자로 TypeMismatchException.class를 주었다.
이 익셉션은 경로 변수값의 타입이 올바르지않을때 발생한는 익셉션이다. 

<u>이 익셉션이 발생하면 에러응답을 보내는 대신 handleTypeMismatchException()메소드를 실행한다.</u>

@ExceptionHandler 애노테이션을 적용한 메서드는 컨트롤러의 요청 매핑 애노테이션적용 메서드와 마찬가지로 뷰 이름을 리턴할 수 있다.

익셉션 객체에 대한 정보를 알고싶다면 메소드의 파라미터로 익셉션 객체를 전달받아 사용하면된다.

```java
@ExceptionHandler(TypeMismatchException.class)
public String handleTypeMismatchException(TypeMismatchException ex){
    // ex사용해서 로그 남기는등 작업
    return "member/invalidId";
}    
```


### 6.1 @ControllerAdvice를 이용한 공통익셉션처리

컨트롤러 클래스에 @ExceptionHandler 애노테이션을 적용하면 해당 컨트롤러에서 발생한 익셉션만을 처리한다.

하지만 다수의 컨트롤러에서 동일 타입의 익셉션이 발생할수도있다. 

이때 익셉션 처리코드가 동일하다면 어떻게 해야할까? 각 컨트롤러 클래스마다 익셉션 처리 메소드를 구현하는 것은 불필요한 코드중복을 발생시킬 뿐더러, 유지보수에 어려워 진다.

여러 컨트롤러에서 동일하게 처리할 익셉션이 발생하면 **@ControllerAdvice 애노테이션**을 이용해서 중복을 없앨수있다.

```java
@ControllerAdvice("spring")
public class CommonExceptionHandler{
    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(){
        return "error/commonException";
    }
}
```

@ControllerAdvice애노테이션이 적용된 클래스는 지정한 범위의 컨트롤러에 공통으로 사용될 설정을 지정할수있다. 

위코드는 "spring"패키지와 그 하위패키지에 속한 컨트롤러 클래스를 위한 공통기능을 정의했다. 

spring패키지와 그 하위패키지에 속한 컨트롤러에서 RuntimeException이 발생하면 handleRuntimeException()메소드를 통해서 익셉션을 처리하게 된다.

물론 @ControllerAdvice적용 클래스가 동작하려면 해당클래스를 스프링에 빈으로 등록해야한다!



### 6.2 @ExceptionHandler 적용 메소드의 우선순위

@ControllerAdvice 클래스에 있는 @ExceptionHandler 메소드와 컨트롤러 클래스에있는 @ExceptionHandler 메소드중 컨트롤러 클래스에 적용된 @ExceptionHandler메소드가 우선한다. 즉 컨트롤러의 메소드를 실행하는 과정에서 익셉션이 발생하면 다음의 순서로 익셉션을 

**처리할 @ExceptionHandler메소드를 찾는다.**

    - 같은 컨트롤러에 위치한 @Exception 메소드중 해당익셉션을 처리할수있는 메소드검색
    - 같은 클래스에 위치한 메소드가 익셉션을 처리할수없는경우 @ControllerAdvice클래스에 
    위치한 @ExceptionHandler메소드 검색


@ControllerAdvice애노테이션은 공통설정을 적용할 컨트롤러 대상을 지정하기위해 아래와같은 속성을 제공한다.

| 속성               | 타입                          | 설명                                            |
| ------------------ | ----------------------------- | ----------------------------------------------- |
| value basePackages | String[]                      | 공통 설정을 적용할 컨트롤러가 속하는 기준패키지 |
| annotations        | Class<? extends Annotation>[] | 특정 애노테이션이 적용된 컨트롤러 대상          |
| assignableTypes    | Class<?>[]                    | 특정타입또는 그 하위 타입인 컨트롤러 대상       |



### 7.3 @ExceptionHandler 애노테이션 적용 메소드의 파라미터와 리턴타입

**@ExceptionHandler애노테이션을 붙인 메소드는 다음파라미터를 가질수있다**

- HttpServletRequest, HttpServletResponse, HttpSession
- Model
- 익셉션

**리턴 가능한 타입은 다음과같다**

- ModelAndView
- String (뷰 이름)
- (@ResponseBody 애노테이션을 붙인경우) 임의객체
- ResponseEntity

