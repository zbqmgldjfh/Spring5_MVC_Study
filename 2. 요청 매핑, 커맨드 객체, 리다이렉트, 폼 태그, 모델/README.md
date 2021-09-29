# MVC1: 요청매핑, 커맨드 객체, 리다이렉트, 폼 태그, 모델



## 1. 요청 매핑 애노테이션을 이용한 경로 매핑

##### 프로젝트 설정은 직접 pom.xml과 web.xml을보자
웹 어플리케이션을 개발하는 것은 다음코드를 작성하는 것이다.

- 특정 요청 URL을 처리할 코드

- 처리결과를 HTML과 같은 형식으로 응답하는 코드


이 중 첫번째는 @Controller 애노테이션을 사용한 컨트롤러 클래스를 이용해서 구현한다.

컨트롤러 클래스는 요청 매핑 애노테이션을 사용해서 메소드가 처리할 요청경로를 지정한다.

요청 매핑 애노테이션에는 @RequestMapping, @GetMapping, @PostMapping 등이 있다.



앞서 HelloController 클래스는 다음과 같이 @GetMapping애노테이션을 사용해 "/hello"요청경로를 hello() 

메소드가 처리하도록 설정했었다.

```java
@Controller
public class HelloController{
    @GetMapping("/hello")
    public String hello(Model model,
    @RequestParam(value="name",required=false) String name){
        model.addAttribute("greeting","안녕하세요," +name);
        return "hello";
    }
}
```
요청 매핑 애노테이션을 적용한 메소드를 두개이상 정의할수있다.



예를 들어 회원가입과정을 생각하면 일반적인 회원가입과정은 '약관동의'->'회원정보입력'->'가입완료'인데 각과정을 위한 URL을 다음과 같이 정할수있다.

- 약관동의화면 : http://localhost:8080/sp5-mvc/register/step1
- 회원정보입력화면 :  http://localhost:8080/sp5-mvc/register/step2
- 가입처리결과화면 :  http://localhost:8080/sp5-mvc/register/step3

여러 단계를 거쳐 하나의 기능이 완성되는 겨우 관련 요청 경로를 한개의 컨트롤러 클래스에서 처리하면 코드관리가 효율적이다.



다음과 같이 회원가입 과정을 처리하는 컨트롤러 class를 한개만 만들고 3개의 메소드에 각요청 경로를 처리하도록 구현할수있다.

```java
@Controller
public class RegistController{
    
    @RequestMapping("/register/step1")
    public String handleStep1(){
        return "register/step1";
    }

    @RequestMapping("/register/step2")
    public String handleStep2(){
        ...
    }
    @RequestMapping("/register/step3")
    public String handlerStep3(){
        ...
    }
}
```

위의 코드를 보면 각요청 매핑 애노테이션 경로가 "/register"로 시작한다. 

이경우 아래의 코드처럼 공통되는 부분경로를 다음 @RequestMapping 애노테이션을 클래스에 적용하고 각메소드는 나머지경로를 값으로 갖는 요청매핑애노테이션을 적용할수있다.

```java
@Controller
@RequestMapping("/register")
public class RegistController{
    
        @RequestMapping("/step1")
        public String handleStep1(){
            return "register/step1";
        }

        @RequestMapping("/step2")
        public String handleStep2(){
            ...
        }
        @RequestMapping("/step3")
        public String handlerStep3(){
            ...
        }
    }
```

스프링 MVC는 클래스에 적용한 요청 매핑 애노테이션의 경로와 메소드에 적용한 요청 매핑 애노테이션의 경로를 합쳐서 경로를 찾기 때문에 위 코드에서 handleStep1()메소드가 처리하는 경로는 "/step1"이 아닌 **"/register/step1"**이된다.



다음 예제코드를 보자. (회원가입의 첫번째 과정인 약관보여주는 컨트롤러클래스)

```java
@Controller
public class RegisterController{
    @ReqeustMapping("/register/step1")
    public String handleStep1(){
        return "register/step1";
    }
}
```

위의 코드는 단순하게 약관만을 보여주는 과정이다.

/register/step1 으로 요청하면 다음 JSP를 랜더링하여 사용자에게 보여주면 된다.

```jsp
// register/step1.jsp
<%@ page contentType="text/html; charset=utf-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>회원가입</title>
</head>
<body>
    <h2>약관</h2>
    <p>약관 내용</p>
    <form action="step2" method="post">
    <label>
        <input type="checkbox" name="agree" value="true"> 약관 동의
    </label>
    <input type="submit" value="다음 단계" />
    </form>
</body>
</html>
```


이제 남은작업은 ControllerConfig파일을 작성하고 이파일에 RegisterController 클래스를 빈으로 등록하는것이다

```java
@Configuration
public class ControllerConfig{
    
    @Bean
    public RegisterController registerController(){
        return new RegisterController();
    }
}
```



이제 Tomcat 서버를 가동시켜서 잘 출력되는지 확인해보장

    http://localhost:8080/sp5-chap11/register/step1

<img src="C:\Users\JiWoo\Desktop\화면 캡처 2021-09-29 104237.png" alt="화면 캡처 2021-09-29 104237" style="zoom: 80%;" />



## 2.GET과 POST구분: @GetMapping, @PostMapping
위의 html 폼코드를 보면 전송방식을 post로 지정했었다. 주로 폼을 전송할때에 Post방식을 사용하는데 스프링 MVC는 별도설정이 없으면 GET과 POST방식에 상관없이 @RequestMapping에 지정한 경로와 일치하는 요청을 처리한다.

만약 Post방식 요청만 처리하고 싶다면 다음과 같이 **@PostMapping**을 사용해서 제한할수있다.

```java
@Controller
public class RegisterController{
    @PostMapping("/register/step2")
    public String handleStep2(){
        return "register/step2";
    }
}
```
위와 같이 설정하면 handleStep2()는 POST방식의 "/register/step2"요청 경로만 처리하며 GET방식의 "/register/step2" 요청 경로는 처리하지 않는다. 

동일하게 @GetMapping애노테이션을 사용하면 GET방식만 처리하도록 제한할 수 있다.

이 두 애노테이션을 사용하면 다음코드처럼 같은 경로에 대해 GET과 POST를 각각 다른메소드가 처리하도록 설정할수있다.

```java
@Controller
public class LoginController{
    @GetMapping("/member/login")
    public String form(){
        ...
    }

    @PostMapping("/member/login")
    public String login(){
        ...
    }
}    
```


## 3.요청 파라미터 접근
약관 동의 화면을 생성하는 step1.jsp 코드를 보면 다음처럼 약관에 동의할 겨우 값이 true인 'agree'요청 파라미터의 값을 POST방식으로 전송한다.

따라서 폼에서 지정한 agree 요청 파라미터의 값을 이용해서 약관동의여부를 확인할 수 있다.

```html
<form action="step2" method="post">
<label>
	<input type="checkbox" name="agree" value="true"> 약관 동의
</label>
<input type="submit" value="다음 단계" />
</form>
```
컨트롤러 메소드에서 요청 파라미터를 사용하는 첫번째 방법은 **HttpServletRequest를 직접이용**하는 것이다.



예를들면 다음과 같이 컨트롤러 처리 메소드의 파라미터로 HttpServletRequest타입을 사용하고 HttpServletRequest의 getParameter()메소드를 이용해서 파라미터의 값을 구하면된다.

```java
@Controller
public class RegisterController{
    
    @RequestMapping("/register/step1")
    public String handleStep1(){
        return "register/step1";
    }
    
    @PostMapping("/register/step2")
    public String handleStep2(HttpServletRequest request){
        String agreeParam = reuqest.getParameter("agree");
        if(agreeParam==null || !agreeParam.equals("true")){
            return "register/step1";
        }
        return "register/step2";
    }
    
}
```
요청 파라미터에 접근하는 또다른 방법은 **@ReuqestParam** 을 사용하는 것이다.

요청 파라미터 개수가 몇개 안되면 이 애노테이션을 사용해서 간단하게 요청파라미터의 값을 구할 수 있다.



다음은 위의 코드를 @RequestParam 애노테이션을 사용해서 구한코드다.

```java
@Controller
public class RegisterController{
    ...
    @PostMapping("/register/step2")
    public String handleStep2(
        @RequestParam(value="agree",defaultVaue="false") Bollean agreeVal){
            if(!agree)
            return "register/step1";
            return "register/step2";
        }
}
```

agree는 요청파라미터의 값을 읽어와 agreeVal 파라미터에 할당하고있다.
요청파라미터의 값이 없으면 default로 "false"문자열을 값으로 사용할거라 지정하였다.



### @RequestParam 애노테이션은 아래의 속성을 제공한다.

| 속성           | 타입    | 설명                                                         |
| -------------- | ------- | ------------------------------------------------------------ |
| `value`        | String  | HTTP요청 파라미터의 이름을 지정한다.                         |
| `required`     | boolean | 필수여부를 지정한다. 이값이 true이면서 해당요청파라미터에 값이 없으면 익셉션이발생. 기본값은 true이다 |
| `defaultValue` | String  | 요청 파라미터가 값이 없을때 사용할 문자열 값을 지정한다. 기본값은없다 |



@RequestParam애노테이션을 사용한 코드를 보면 다음과 같이 agreeVal파라미터의 타입이 Boolean이다.

    @RequestParam(value="agree",defaultValue="false") Boolean agreeVal

스프링 MVC는 파라미터 타입에 맞게 String 값을 변환해준다. 

위코드는 agree요청 파라미터의 값을 읽어와 Boolean타입으로 변환해서 agreeVal파라미터에 전달한다. 

Boolean타입외에 int,long,Integer,Long등 기본데이터 타입과 래퍼타입에 대한 변환을 지원한다.



이제 약관동의 여부확인을 위해 HTTP요청 파라미터의 값을 사용할수있게되었으니 약관동의 화면 다음요청을 처리하는 코드를 RegisterController클래스에 추가하자.

```java
@Controller
public class RegisterController{
    
    ...handleStep1()메소드 생략
        
    @PostMapping("/register/step2")
    public String handleStep2(
        @RequestParam(value="agree",defaultValue = "false") Boolean agree){
    if(!agree)
        return "register/step1";
    }
    return "register/step2";
}
```
handleStep2()는 agree요청 파라미터의 값이 true가 아니면 다시약관동의 폼을 보여주기위해 "register/step1" 뷰이름을 리턴한다. 약관에동의했다면 "register/step2"를 뷰이름으로 리턴한다. 



아래의 코드는 "register/step2" 뷰에 해당하는 jsp코드다.    

```jsp
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html>
<head>
    <title>회원가입</title>
</head>
<body>
<h2>회원 정보 입력</h2>
<form action="step3" method="post">
    <p>
        <label>이메일:<br>
        <input type="text" name="email" id="email">
        </label>
    </p>
    <p>
        <label>이름:<br>
        <input type="text" name="name" id="name">
        </label>
    </p>
    <p>
        <label>비밀번호 확인:<br>
        <input type="password" name="confirmPassword" id="confirmPassword">
        </label>
    </p>
    <input type="submit" value="가입 완료">
</form>

</body>
</html>
```



## 4. 리다이렉트 처리

웹브라우저에서 http://localhost:8080/10_MVC1_war/register/step2 주소를 직접 입력하면 에러화면이 출력된다.

RegisterController 클래스의 handleStep2()은 POST방식만을 처리하기 때문에 웹 브라우저에 직접 주소를 입력할때 사용되는 GET방식요청은 처리하지 않는다. 

스프링 MVC는 handleStep2() 메소드가 GET요청의 처리를 지원하지 않으므로 405상태코드를응답하게된다.



컨트롤러에서 특정페이지로 리다이렉트 시키는법은 간단하다. "redirect:경로"를 뷰이름으로 리턴하면된다.

/register/step2경로를 GET방식으로 접근할때 약관동의화면인 /register/step1경로로 리다이렉트 시키고 싶다면 아래와같이step2get()메소드를 추가해주면 된다.

```java
@Controller
public class RegisterController {
    ..생략
    @GetMapping("/register/step2")
    public String handleStep2Get(){
        return "redirect:/register/step1";
    }
}
```

@RequestMapping, @GetMapping 등 요청 매핑 관련 애노테이션을 적용한 메소드가 "redircet:"로 경로를 리턴하면 나머지 경로를 이용해서 리다이렉트할 경로를구한다. 

"redirect:"뒤의 문자열이 "/"로 시작하면 웹어플리케이션을 기준으로 이동경로를 생성한다.

예를들어 위에서 뷰값으로 "redirect:/register/step1"을 사용했는데 이경우 이동경로가 "/"로 시작하므로 실제 리다이렉트할 경로는 웹 어플리케이션 경로인 "sp5-chap11/register/step2"가 됩니다.



## 5. 커맨드 객체를 이용해서 요청 파라미터 사용하기
step2.jsp가 생성한 form은 다음 파라미터를 이용해서 정보를 서버에 전송하게 된다.

- email
- name
- password
- confirmPassword



폼 전송요청을 처리하는 컨트롤러 코드는 각파라미터의 값을 구하기 위해 다음과 같은 코드를 사용할수있다.

```java
@PostMapping("/register/step3")
public String handleStep3(HttpServletRequest request){
    String email=request.getParameter("email");
    String name=request.getParameter("name");
    String password=request.getParameter("password");
    String confirmPassword=request.getParameter("confirmPassword");

    RegisterRequest regReq=new RegisterRequest();
    regReq.setEmail(email);
    regReq.setName(name);
    ...
}
```
위 코드가 올바르게 동작하지만, 요청 파라미터 개수가 증가할때마다 handleStep3()메소드의 코드 길이도 함께 길어지는 단점이있다.

파라미터 개수가 20개가 넘는 복잡한폼은 파라미터의 값을 읽어와 설정하는 코드만 40줄이상작성해야한다.



스프링은 이런 불편함을 줄이기 위해 요청 파라미터의 값을 **커맨드객체**에 담아주는 기능을제공한다.



예를들어 이름이 name인 요청 파라미터의 값을 커맨드 객체의 setName()메소드를 사용해서 커맨드 객체에 전달하는 기능을 제공한다.

커맨드 객체라고 해서 특별한 코드를 작성해야하는것은아니다. 요청파라미터의 값을 전달받을 수 있는 setter를 포함하는 객체를 커맨드 객체로 사용하면된다. 



커맨드 객체는 다음과같이 요청매핑애노테이션이 적용된 메소드의 파라미터에 위치한다.

```java
@PostMapping("/register/step3")
public String handleStep3(RegisterRequest regReq){
    ...
}
```
RegisterRequest 클래스에는 setEmail(), setName(), setName(), setPassword(), setConfirmPassword() 메소드가 있다.



스프링은 이들 메소드를 사용해서 email, name, password, confirmPassword요청 파라미터의 값을 커맨드 객체에 복사한뒤 regReq파라미터로 전달하게 된다.

##### 즉! 스프링MVC가 handleStep3() 메소드에 전달할 RegisterRequest 객체 A를 생성하고  A객체의 setter를 이용해서 regReq 요청파라미터에게 전달하게 되는 것이다.
폼에 입력한 값을 커맨드 객체로 전달받아 회원가입을 처리하는 코드를 보자.

```java
@Controller
public class RegisterController {
    
    private MemberRegisterService memberRegisterService;
    
    public void setMemberRegisterService(
            MemberRegisterService memberRegisterService){
        this.memberRegisterService=memberRegisterService;
    }
    
    // 중략
    
    @PostMapping("/register/step3")
    public String handleStep3(RegisterRequest regReq){
        try{
            memberRegisterService.regist(regReq);
            return "register/step3";
        }catch (DuplicateMemberException ex){
            return "register/step2";
        }
    }
}
```
handleStep3()메소드는 DI받은 MemberRegisterService를 이용해서 회원가입을 처리한다.

회원가입에 성공하면 뷰이름을 "register/step3"를 리턴하고, 이미 동일한 이메일 주소를 가진 회원 데이터가 존재하면 뷰이름으로 "register/step2"를 리턴해서 다시폼을 보여줍니다.

RegisterController 클래스는 MemberRegisterService 타입의 빈을 의존하므로 ControllerConfig.java파일에 아래와 같이 **Dependency Injection(DI)**을 설정합니다.

```java
@Configuration
public class ControllerConfig {
    
    @Autowired private MemberRegisterService memberRegSvc;
    
    @Bean
    public RegisterController registerController() {
        RegisterController controller = new RegisterController();
        controller.setMemberRegisterService(memberRegSvc);
        return controller;
    }
}
```



```jsp
//step3.jsp
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>회원가입</title>
</head>
<body>
    <p>회원 가입을 완료했습니다.</p>
    <p><a href="<c:url value='/main'/>">[첫 화면 이동]</a></p>
</body>
</html>
```



## 6. 뷰 JSP코드에서 커맨드 객체 사용하기

가입할 때 사용한 이메일 주소와 이름을 회원 가입 완료 화면에서 보여주면 사용자에게 조금 더 친절하게 보일것이다.

HTTP요청 파라미터를 이용해서 회원정보를 전달했으므로 JSP의 표현식 등을 이용해서 정보를 표시해도 되지만, <u>커맨드 객체를 사용해서</u> 정보를 표시할수도있다. 



앞서 작성했던 step3.jsp코드를 수정해보았다!

```jsp
//step3.jsp
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>회원가입</title>
</head>
<body>
    <p><strong>${registerRequest.name}님</strong>
        회원 가입을 완료했습니다.</p>
    <p><a href="<c:url value='/main'/>">[첫 화면 이동]</a></p>
</body>
</html>   
```

위의 코드를보면 ${registerRequest.name}코드가 있다. JSP 액션태그를 사용하는 부분이다.

여기서 registerRequest가 커맨드 객체에 접근할때 사용한 **속성이름**이다. 

스프링MVC는 커맨드 객체의(첫 글자를 소문자로 바꾼) 클래스 이름과 동일한 속성이름을 사용해서 커맨드 객체를 뷰에 전달한다.

커맨드객체의 클래스이름이 RegisterRequest인 경우 JSP코드는 위에서처럼 registerRequest 라는 이름을 사용해서 커맨드 객체에 접근할수있는 것 이다.



### 커맨드 객체와 뷰 모델 속성의 관계

![image](https://user-images.githubusercontent.com/40031858/87149248-d59a0c80-c2ea-11ea-99b8-742a3236998b.png)

## 7. @ModelAttribute 애노테이션으로 커맨드 객체속성이름변경
커맨드 객체에 접근할 때 사용할 속성 이름을 변경하고 싶다면 커맨드 객체로 사용할 파라미터에**@ModelAttribute**을 적용.

```java
@PostMapping("/register/step3")
public String handleStep3(@ModelAttribute("formData") RegisterRequest regReq){
    ...
} 
```

@ModelAttribute 애노테이션은 모델에서 사용할 속성이름을 값으로 설정한다. 

위설정을 사용하면 뷰 코드에서 "formData"라는 이름으로 커맨드 객체에 접근할수잇다.



## 8. 커맨드 객체와 스프링 폼 연동

회원정보 입력폼에서 중복된 이메일 주솔르 입력하면 텅 빈폼을보여준다. 폼이 비어있으므로 입력한 값을 다시입력해야 하는 불편함이 따른다. 

##### 다시 폼을 보여줄 때 커맨드 객체의 값을 폼에 채워주면 이런 불편함을 해소할수있다.
    <input type="text" name="email" id="email" value=${registerRequest.email}">
    ...
    <input type="text" name="name" id="name" value="${registerRequest.name}">



스프링MVC가 제공하는 커스텀 태그를 사용하면 좀더 간단하게 커맨드 객체의 값을 출력할수있다.

스프링은 <form:form> 태그와 <form:input> 태그를 제공하고 있다. 이두태그를 사용하면 아래와 같이 커맨드 객체의 값을 폼에 출력가능해진다.

```jsp
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html>
<head>
    <title>회원가입</title>
</head>
<body>
<h2>회원 정보 입력</h2>
<form:form action="step3" modelAttribute="registerRequest">
    <p>
        <label>이메일:<br>
            <form:input path="email" />
        </label>
    </p>
    <p>
        <label>이름:<br>
            <form:input path="name" />
        </label>
    </p>
    <p>
        <label>비밀번호:<br>
            <form:password path="password" />
        </label>
    </p>
    <p>
        <label>비밀번호 확인:<br>
            <form:password path="confirmPassword" />
        </label>
    </p>
    <input type="submit" value="가입 완료">
</form:form>
<form:form>태그의 속성은 다음과같습니다
```
- action: < form>태그의 action속성과 동일한값을사용
- modelAttribute: 커맨드 객체의 속성이름을 지정한다. 설정하지않는 경우 "command"를 기본값으로 사용한다. 예제에서 커맨드 객체의 이름은 "registerRequest"이니, 이름을 registerRequest 로 설정했다.



< form:input> 태그는 < input>태그를 생성한다. 

path로 지정한 커맨드 객체의 프로퍼티를 < input> 태그의 value속성값으로사용한다.

예를들어 < form:input path="name"/>은 커맨드 객체의 name 프로퍼티값을 value속성으로 사용한다.



만약 커맨드 객체의 name프로퍼티 값이 "스프링" 이었다면 다음과 같은 < input 태그를생성한다> 

    <input id="name" name="name" type="text" value="스프링"/>

< form:password> 태그도 < form:input>태그와 유사하다. password타입의 < input>태그를 생성하므로 value속성의 값을 빈문자열로 설정한다.

< form:form>태그를 사용하려면 **사전에 커맨드 객체가 존재해야한다**. 

step2.jsp에서 < form:form> 태그를 사용하기 때문에 <u>step1에서 step2로 넘어오는 단계</u>에서 이름이 "registerRequest"인 객체를 Model에 등록해야 < form:form>태그가 정상작동한다.

```java
//RegisterController.java
@Controller
public class RegisterController{
    ...
    @PostMapping("/register/step2")
    public String handleStep2(
        @RequestParam(value="agree",defaultValue="false") Boolean agree, Model model){
            if(!agree){
                return "register/step1";
            }
            model.addAttribte("registerRequest",new RegisterRequest());
            return "register/step2";
        }
        ...
}
```



## 9. 컨트롤러구현없는 경로매핑

##### step3.jsp코드를 보면 다음코드를 볼 수 있다.
    <p><a href="<c:url value='/main'/>">[첫 화면 이동]</a></p>
step3.jsp는 회원가입 완료후 첫화면으로 이동할 수 있는 링크를 보여준다. 이 첫화면은 단순히 환영 문구와 회원가입으로 이동할 수 있는링크만제공 한다고 하자. 



WebMvcConfigurer인터페이스의 addViewControllers()메소드를 사용하면 된다!

이 메소드를 재정의하면 컨트롤러 구현없이 다음의 간단한 코드로 요청경로와 뷰이름을 연결할수있다.

```java
//MvcConfig 에 추가.
@Override
public void addViewControllers(ViewControllerRegistry registry){
    registry.addViewController("/main").setViewName("main");
}     
```


```jsp
//main.jsp
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>메인입니당</title>
</head>
<body>
    <p>환영합니다.</p>
    <p><a href="<c:url value="/register/step1"/>">[회원가입하기]</a>
</body>
</html>
```
http://localhost:8080/sp5-chap11/main <= 이 링크에서 메인페이지를 볼 수 있다.



## 10. 커맨드객체 : 중첩 , 콜렉션 프로퍼티
##### 세개의 설문항목과 응답자의 지역과나이를 입력받는 설문조사를위해 클래스를작성하자.

우선 설문항목은 다음과 같다.

```java
public class Respondent {
    
    private int age;
    private String location;

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
```

조사 데이터 반환


```java
public class AnsweredData {
    
    private List<String> responses;
    private Respondent res;

    public List<String> getResponses() {
        return responses;
    }

    public void setResponses(List<String> responses) {
        this.responses = responses;
    }

    public Respondent getRes() {
        return res;
    }

    public void setRes(Respondent res) {
        this.res = res;
    }
}
```
Respondent 클래스는 응답자정보를담고 AnsweredData클래스는 설문항목답변과 응답자 정보를받는다.

AnsweredData클래스는 답변목록을 저장하기위해 List타입의 responses프로퍼티를 사용하고,

응답자 정보를 담기위해 Respondent타입의 res프로퍼티사용하고 있다.



AnsweredData 클래스는 앞서 커맨드 객체로 사용한 클래스와 비교하면 다음차이가있다.
- 리스트 타입의 프로퍼티가 존재한다. responses프로퍼티는 String타입의 값을 갖는 List콜렉션이다.
- 중첩 프로퍼티를 갖는다.res프로퍼티는 Respondent타입이며 res프로퍼티는 다시age와 location 프로
퍼티를 갖는다. 이를 중첩된 형식으로 표시하면 res.age 프로퍼티나 res.location프로퍼티로 표현할수있다.



스프링MVC는 커맨드 객체가 리스트 타입의 프로퍼티를 가졌거나 중첩프로퍼티를 가진경우에도 요청 파라미터의 값을 알맞게 커맨드 객체에 설정해주는 기능을제공하는데 규칙은다음과같다.
- HTTP요청파라미터 이름이 "프로퍼티이름[인덱스]"형식이면 List타입 프로퍼티의 값목록으로 처리
- HTTP요청파라미터 이름이 "프로퍼티이름.프로퍼티이름"과 같은형식이면 중첩프로퍼티값을 처리.

예를들어 이름이 responses이고 List타입인 프로퍼티를 위한 요청 파라미터의 이름으로 "responses[0]",
"responses[1]"을 사용하면 각각 0번인덱스와 1번인덱스의 값으로 사용된다.

중첩 프로퍼티의 경우 파라미터 이름을 "res.name"로 지정하면 다음과 유사한 방식으로 커맨드 객체에 파라미터의 값을 설정한다.



#### AnsweredData클래스를 커맨드객체로 사용하는 예제를 작성하자. 

```java
@Controller
@RequestMapping("/survey")
public class SurveyController {
    
    @GetMapping
    public String form(){
        return "survey/surveyForm";
    }
    
    @PostMapping
    public String submit(@ModelAttribute("ansData") AnsweredData data){
        return "survey/submitted";
    }
}
```

form()메소드와 submit()메소드의 요청 매핑 애노테이션은 전송 방식만을 설정하고 클래스의 @RequestMapping에만 경로를 지정한다. 

이경우 form()메소드와 submit() 메소드가 처리하는 경로는 /survey가된다. 즉 form()메소드는 GET방식의
"/survey" 요청을 처리하고 submit()메소드는 POST방식의 "/survey"요청을 처리한다.

submit()메소드는 커맨드 객체로 AnsweredData 객체를사용.

```java
//이후에 ControllerConfig에 빈을 추가
@Bean
public SurveyController surveyController(){
	return new SurveyController();
}
```



```jsp
///surveyForm.jsp
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>설문조사</title>
</head>
<body>
    <h2>설문조사</h2>
    <form method="post">
    <c:forEach var="q" items="${questions}" varStatus="status">
    <p>
        ${status.index + 1}. ${q.title}<br/>
        <c:if test="${q.choice}">
            <c:forEach var="option" items="${q.options}">
            <label><input type="radio" 
                        name="responses[${status.index}]" value="${option}">
                ${option}</label>
            </c:forEach>
        </c:if>
        <c:if test="${! q.choice }">
        <input type="text" name="responses[${status.index}]">
        </c:if>
    </p>
    </c:forEach>
    <p>
        1.당신의 역할은? <br/>
        <label>
            <input type="radio" name="responses[0]" value="서버">
            서버개발자
        </label>
        <label>
            <input type="radio" name="responses[0]" value="프론트">
            프론트개발자
        </label>
        <label>
            <input type="radio" name="responses[0]" value="풀스택">
            풀스택개발자
        </label>
```


```jsp
    </p>
        <p>
            2.가장 많이 사용하는 개발도구는?<br/>
            <label>
                <input type="radio" name="responses[1]" value="Eclipse">
                Eclipse
            </label>
            <label>
                <input type="radio" name="responses[1]" value="IntelliJ">
                IntelliJ
            </label>
            <label>
                <input type="radio" name="responses[1]" value="Sublime">
                Sublime
            </label>
        </p>
        <p>
            3.하고싶은말 <br/>
            <input type="text" name="responses[2]">
        </p>
    <p>
        <label>응답자 위치:<br>
        <input type="text" name="res.location">
        </label>
    </p>
    <p>
        <label>응답자 나이:<br>
        <input type="text" name="res.age">
        </label>
    </p>
    <input type="submit" value="전송">
    </form>
</body>
</html>
```

위의 JSP는 너무 하드 코딩했다는 문제점이 있다.



```jsp
//submitted.jsp
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>응답 내용</title>
</head>
<body>
    <p>응답 내용:</p>
    <ul>
        <c:forEach var="response" 
                items="${ansData.responses}" varStatus="status">
        <li>${status.index + 1}번 문항: ${response}</li>
        </c:forEach>
    </ul>
    <p>응답자 위치: ${ansData.res.location}</p>
    <p>응답자 나이: ${ansData.res.age}</p>
</body>
</html>
```



## 12. Model을 통해 컨트롤러에서뷰에 데이터 전달하기    

컨트롤러는 뷰가 응답화면을 구성하는데 필요한 데이터를 생성해서 전달해야한다. 이때 사용하는것이 Model이다.

```java
@Controller
public class HelloController{
    
    @RequestMapping("/hello")
    public String hello(Model model,
    @RequestParam(value="name",required=false) String name){
        model.addAttribute("greeting","안녕하세요,"+name);
        return "hello";
    }
}
```
##### 뷰에 데이터를 전달하는 컨트롤러는 hello()메소드처럼 다음 두가지를 하면된다.
- 요청 매핑 애노테이션이 적용된 메소드의 파라미터로 Model추가
- Model 파람터의 addAttribute()메소드로 뷰에서 사용할 데이터 전달.



addAttribute()메소드의 첫번째 파라미터는 속성이름이다. 뷰코드는 이이름을 사용해서 데이터에 접근한다.

JSP는 다음과같이 표현식을사용해서 속성값을 접근할수 있다.

    ${greeting}
앞서 SurveyForm.jsp에 설문항목을 하드코딩했었는데 설문항목을 컨트롤러에서 생성해서 뷰에 전달하는 방식으로 변경해보자.

```java
//Question
public class Question {
private String title;
private List<String> options;

public Question(String title, List<String> options) {
    this.title = title;
    this.options = options;
}
public Question(String title){
    this(title, Collections.<String>emptyList());
}
public String getTitle() {
    return title;
}
public List<String> getOptions() {
    return options;
}
public boolean isChoice(){
    return options!=null && !options.isEmpty();
    }
}
```

Question이 객관식 이라면 질문만 있을태니 options를 null로 갖는 생성자를 이용하면 된다.



원래였으면 질문을 다른곳에서 만들어야 하는데, 우선 그냥 컨트롤러 안에서 질문을 생성해 주었다.

다음 코드를 봐보자!


```java
//SurveyController

@Controller
@RequestMapping("/survey")
public class SurveyController {
    
    @GetMapping
    public String form(Model model){
        List<Question> questions=createQuestions();
        model.addAttribute("questions",questions);
        return "survey/surveyForm";
    }
    
    private List<Question> createQuestions(){
        Question q1=new Question("당신의 역할은 무엇입니깡?",
                Arrays.asList("서버","프론트","풀스택"));
        Question q2=new Question("많이사요하는 개발도구는 무엇입니깡?",
                Arrays.asList("이클립스","인텔리제이","서브라임"));
        Question q3=new Question("하고싶은말을 적어달라능");
        return Arrays.asList(q1,q2,q3);
    }
    
    @PostMapping
    public String submit(@ModelAttribute("ansData") AnswereData data){
        return "survey/submitted";
    }
}
```


```jsp
//surveyFrom.jsp (모델통해 전달받은 Question리스트 이용해 폼생성)
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>설문조사</title>
</head>
<body>
<h2>설문조사</h2>
<form method="post">
    <c:forEach var="q" items="${questions}" varStatus="status">
        <p>
                ${status.index + 1}. ${q.title}<br/>
            <c:if test="${q.choice}">
                <c:forEach var="option" items="${q.options}">
                    <label><input type="radio"
                                name="responses[${status.index}]" value="${option}">
                            ${option}</label>
                </c:forEach>
            </c:if>
            <c:if test="${! q.choice }">
                <input type="text" name="responses[${status.index}]">
            </c:if>
        </p>
    </c:forEach>
    <p>
        <label>응답자 위치:<br>
            <input type="text" name="res.location">
        </label>
    </p>
    <p>
        <label>응답자 나이:<br>
            <input type="text" name="res.age">
        </label>
    </p>
    <input type="submit" value="전송">
</form>
</body>
</html>
```



## 12.1 ModelAndView를 통한 뷰선택과 모델전달

##### 지금까지 구현한 컨트롤러는 두가지특징이있다.
- Model을 이용해서 뷰에 전달할 데이터 설정
- 결과를 보여줄 뷰 이름을 리턴



ModelAndView를 사용하면 이 두가지를 한번에 처리할수있다. 요청매핑 애노테이션을 적용한 메소드는 String 타입 대신 **ModelAndView**를 리턴할수있다.

ModelAndView는 모델과 뷰이름을 함께 제공한다. 

다음과같이 ModelAndView클래스를 이용해 SurveyController 클래스의 form()메소드를 구현할수있다.

```java
@GetMapping
public ModelAndView form(){
    List<Question> questions=createQuestions();
    ModelAndView mav=new ModelAndView();
    mav.addObject("questions",questions);
    mav.setViewName("survey/surveyForm");
    return mav;
}
```

뷰에 전달할 모델 데이터는 addObject()메소드로 추가한다. 뷰이름은 setViewName()메소드를 이용해지정.



## 12.2 GET방식과 POST방식에 동일이름 커맨드객체 사용하기

< form:form> 태그는 사용하려면 커맨드 객체가 반드시 존재해야한다. 최초에 폼을 보여주는 요청에 대해 

< form:form>태그를 사용하려면 폼표시 요청이 왔을때에도 커맨드 객체를 생성해서 모델에 저장해야한다.



##### 이를 위해 RegisterController클래스의 handleStep2() 메소드는 다음과 같이 Model에 직접 객체를추가했다.

```java
커맨드객체를 파라미터로 추가한결과
@PostMapping("/register/step2")
public String handleStep2(
        @RequestParam(value="agree", defaultValue = "false") Boolean agree, RegisterRequest registerRequest){
    if(!agree)
        return "register/step1";
    return "register/step2";
}
```

이름을 명시적으로 지정하려면 @ModelAttribute애노테이션을 사용한다. 

예를들어 "/login"요청 경로일때 GET방식이면 로그인 폼을 보여주고 POST방식이면 로그인을처리하도록 구현한 컨트롤러를 만들어야 한다고 하자. 

입력폼과 전송 처리에서 사용할 커맨드객체의 속성이름이 클래스 이름과 다르다면 GET요청과 POST요청을 처리하는 메소드에@ModelAttribute 애노테이션을 붙인 커맨드 객체를 파라미터로 추가하면된당.

```java
커맨드객체를 파라미터로 추가한결과
@PostMapping("/register/step2")
public String handleStep2(
        @RequestParam(value="agree",defaultValue = "false") Boolean agree,             RegisterRequest registerRequest){
        if(!agree)
            return "register/step1";
        return "register/step2";
    }
```

이름을 명시적으로 지정하려면 @ModelAttribute애노테이션을 사용한다. 

예를들어 "/login"요청 경로일때 GET방식이면 로그인 폼을 보여주고 POST방식이면 로그인을처리하도록 구현한 컨트롤러를 만들어야 한다고 하자. 

입력폼과 전송 처리에서 사용할 커맨드객체의 속성이름이 클래스 이름과 다르다면 GET요청과 POST요청을 처리하는 메소드에@ModelAttribute 애노테이션을 붙인 커맨드 객체를 파라미터로 추가하면된당.

