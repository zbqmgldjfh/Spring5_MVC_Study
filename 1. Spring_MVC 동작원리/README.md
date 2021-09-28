# Spring MVC 핵심 구성요소

우선 MVC의 전반적인 구성에 대해 공부하는 시간입니다. 

이번 내용은 특별히 글로만 진행됩니다. 이론 위주의 내용이기 때문입니다.



## 1. 스프링 MVC 핵심 구성 요소

 다음 그림을 살펴보시죠, 스프링 __MVC의 핵심 구성 요소__ 는 다음과 같습니다
 <img src="https://user-images.githubusercontent.com/60593969/135022231-0096b655-ce81-4ebb-bd9e-9258e60a0591.png" width="800"/>  



위의 사진을 보면 <스프링 빈으로 등록> 이라 되어있는 부분은 스프링 빈으로 등록되어야 하는 것을 의미합니다.

또한 <u>**초록색 박스**의 JSP, 컨트롤러</u>는 개발자가 직접 구현해고 스프링 빈으로 등록해야 하는 요소 입니다.



중앙에 위치한 **DispatcherServlet**은 모든 연결을 담당합니다.

Client로부터 요청이 들어오면 DispatcherServlet은 그 요청을 처리하기 위해서 HandlerMapping 이라는 빈 객체를 통하여 컨트롤러를 검색하게 됩니다. (그림에서 2번 과정)



HandlerMapping은 Clinet의 요청에 따라서 이를 처리할 컨트롤러 빈 객체를 DispatcherServlet에 전달하게 됩니다.



컨트롤러 빈 객체를 DispatcherServlet가 전달받았다고 해서 바로 컨트롤러 객체의 메서드를 실행하는 것 이 아닙니다!



DispatcherServlet은 <u>@Controller 애노테이션을 이용해서 구현한 컨트롤러</u> 뿐만 아니라, 

스프링 2.5까지 주로 사용했었던 <u>Controller 인터페이스를 구현한 컨트롤러</u>, 

특수목적으로 사용되는  <u>HttpRequestHandler 인터페이스를 구현한 클래스</u>를 

**모두 동일한 방식으로 실행할 수 있도록 만들어 집니다**.



위의 3가지 동일한 방식으로 처리하기 위해 중간에 사용되는 것 이 바로! **HandlerAdapter 빈** 입니다!



따라서 DispatcherServlet는 전달받은 컨트롤러 객체를 처리할 수 있는 HandlerAdapter를 찾아 요청을 위임하게 됩니다.  (그림의 3번과정)



HandlerAdapter는 컨트롤러의 알맞은 메서드를 호출하여 요청을 처리하고(4, 5번 과정) 처리결과를 다시 ModelAndView 라는 객체로 변환하여 DispatcherServlet에 반환하게 됩니다.(6번 과정)



이후 DispatcherServlet는 결과를 보여줄 뷰를 찾기위해 ViewResolver 빈 객체를 사용하게 됩니다. (7번 과정)

ModelAndView는 컨트롤러가 반환한 뷰 이름을 담고 있는데, ViewResolver는 이 뷰 이름에 해당하는 view객체를 찾거나 생성해서 리턴하 됩니다.



응답을 생성하기 위해 JSP를 사용하는 ViewResolver는 매번 새로운 View 객체를 생성해서 DispathcerServlet에 리턴합니다.



DispathcerServlet은 ViewResolver 가 리턴한 View 객체에 응답 결과 생성을 요청하게 됩니다. (8번 과정)

JSP를 사용한다면 View 객체는 JSP를 실행할 때 웹 브라우저에 전송할 응답 결과를 생성하고 끝나게 됩니다.

    
## 1-1. 컨트롤러와 핸들러

#### HandlerMapping - 컨트롤러 검색

웹 브라우저로부터 요청이 들어오면 DispatcherServlet은 해당 요청을 처리하기 위한 컨트롤러를 검색하기 위해 HandlerMapping 객체를 이용한다. 



**한가지 의문**이 든다? 컨트롤러를 찾는데 왜 ControllerMapping 이 아니고, __HandlerMapping일까?__

이는 <u>스프링이 범용성이 좋은 프레임 워크이기 때문이다!</u>



@Controller 애노테이션을 붙인 클래스 뿐만 아니라, 자신이 직접 만든 클래스를 이용하여 클라이언트의 요청을 처리할수도 있다. 따라서 DispatcherServlet 입장에서는 클라이언트의 요청을 처리하는 객체가 꼭 @Controller를 적용한 클래스일 필요는 없는 것 이다.



이러한 이유로 스프링 MVC는 웹 요청을 실제로 처리하는 객체를 **Handler**라고 표현한다. 

Controller 인터페이스를 구현한 모든 객체는 스프링 MVC 입장에서는 Handler가 되는 것 이다. 

이런 <u>Handler를 찾아주는 객체가 HandlerMapping이다.</u>



#### HandlerAdapter - 변환

DispatcherServlet은 핸들러 객체의 실제 타입에 상관없이 컨트롤러(핸들러)의 처리 결과를 ModelAndView로만 받으면 된다.

**하지만** 핸들러의 구현 타입에 따라 ModelAndView를 반환하기도 하고, 안하기도 한다.

따라서 핸들러의 처리 결과를 <u>ModelAndView로 변환해주는 객체가 __HandlerAdapter__</u>인 것 이다.



핸들러 객체의 실제 타입마다 그에 맞는 HandlerMapping과 HandlerAdapter가 존재한다.

따라서 핸들러의 종류에 따라 적합한것들을 스프링 bean으로 등록해주어야 한다.

(ps. 스프링이 제공하는 기본 설정을 사용하면 직접 등록하지 않아도 된다)



## 2. DispatcherServlet과 스프링 컨테이너



##### web.xml 파일을 보면 다음과같이 DispatcherServlet의 contextConfiguration 초기화 파라미터를 이용해 설정클래스목록을 전달하고 있습니다.

```xml
<servlet>
    <servlet-name>dispatcher</servletname>
    <servlet-class>
        org.springframework.web.servlet.DispatcherServlet
    </servlet-class>
    <init-param>
        <param-name>contextClass</param-name>
        <param-value>
            org.springframework.web.context.support.AnnotationConfigWebApplicationContext
        </param-value>
    </init-param>
    <init-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>
            config.MvcConfig
            config.ControllerConfig
        </param-value>
    </init-param>
    <load-on-startup>1</load-on-startup>
</servlet>
```

이름이 dispatcher인 servlet은 init-param으로 전달받은 인자를 통하여 스프링 컨테이너를 생성하는데, 

앞에서 언급한 HandlerMapping, HandlerAdpater, 컨트롤러, ViewResolver 등등

 bean은 아래 그림처럼 DispatcherServlet이 생성한 스프링 컨테이너를 통해 구하게 된다.

따라서 DispatcherServlet이 사용하는 설정파일에 이들 빈에 대한 정의가 포함되어 있어야한다.
<img src="https://user-images.githubusercontent.com/60593969/135022668-18d6fa4a-5da8-47e4-b42f-3ee47f383221.png" width=800 />



DispatcherServlet은 초기화 과정에서 <u>contextConfiguration 초기화 파라미터</u>에 지정한 설정파일인 

MvcConfig, ControllerConfig 클래스를 이용하여 스프링 컨테이너를 생성한다.



**MvcConfig**는 다음과 같다.

```java
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer{
    @Override
    public void configureDefaultServletHandling(
        DefaultServletHandlerConfigurer configurer){
            configurer.enable();
        }
    )
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry){
        registry.jsp("/WEB-INF/view/",".jsp");
    }
}
```

위 class를 통하여 **DefaultServlet**과 **ViewResolver**를 초기화 한다.



## 3. @Controller를 위한 HandlerMapping과 HandlerAdapter



DispatcherServlet은 웹 브라우저의 요청을 처리할 handler 객체를 찾기위해 HandlerMapping을 사용하고,

handler를 실행하기 위해 HandlerAdapter를 사용한다.



스프링 컨테이너는 HandlerMapping과 HandlerAdapter 타입의 빈을 사용하므로 핸들러에 알맞은 빈이 스프링 설정에 등로되있어야 한다. 



하지만 위에서 본 MvcConfig를 보면 이에대한 설정 부분이 없다.

이는 전지전능한 **@EnableWebMvc** 덕분이다.

이 태그가 추가해주는 class 중에는 @Controller 타입의 핸들러 객체를 처리하기 위한 클래스도 포함되어있다.

```
o.s.w.servlet.mvc.method.annotation.RequestMappingHandlerMapping
o.s.w.servlet.mvc.method.annotation.RequestMappingHandlerAdpater
```

(o.s.w 는 org.springframework.web 의 줄인 표시이다.)



RequestMappingHandlerMapping은 @Controller 가 적용된 객체의 요청 매핑 애노테이션(@GetMapping 등등) 값을 이용하여 요청을 처리할 Controller Bean을 찾게된다.



RequestMappingHandlerAdapter는 컨트롤러 메서드의 결과값이 String 타입이면 해당 String을 뷰의 이름으로갖는 ModelAndView 객체를 생성해서 DispatcherServlet에게 반환해준다.



## 4. WebMvcConfigurer 인터페이스와 설정

**@EnableWebMvc** 를 사용하면 컨트롤러를 위한 설정을 생성함을 직전에 설명하였다.

또한 @EnableWebMvc를 사용하면 WebMvcConfigurer 타입의 빈을 이용해서 MVC 설정을 추가로 생성한다.



설정 코드를 다시 살펴보자.

```java
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer{
    @Override
    public void configureDefaultServletHandling(
        DefaultServletHandlerConfigurer configurer){
            configurer.enable();
        }
    )
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry){
        registry.jsp("/WEB-INF/view/",".jsp");
    }
}
```

여기서 설정 클래스는 WebMvcConfigurer 인터페이스를 상속받고 있다.

@Configuration이 붙으면 스프링 빈으로 등록되니 MvcConfig 클래스는 <u>WebMvcConfigurer 타입의 빈</u>이 된다.



@EnableWebMvc 애노테이션을 사용하면 WebMvcConfigurer 타입인 빈 객체의 메소드를 호출해서 MVC설정을 추가한다. 

예를 들어 ViewResolver 설정을 추가하기 위해 WebMvcConfigurer
타입인 빈객체의 configureViewResolvers()메소드를 호출한다.
따라서 WebMvcConfigurer 인터페이스를 구현한 설정클래스는 configureViewResolvers() 메소드를
재정의해서 알맞은 뷰관련설정을 추가하면된다.



스프링 5는 자바8 부터 지원하는 default메소드를 사용해 WebMvcConfigurer 인터페이스의 메소드에 기본구현을 제공하고있다. 

다음은 스프링5가 지원하는 WebMvcConfigurer인터페이스의 일부구현코드다.

```java
public interface WebMvcConfigurer{
    
    default void configurerPathMatch(PathMatchConfigurer configurer){
    }
    
    default void configureDefaultServletHandling(
        DefaultServletHandlerConfigurer configurer){
    }

    default void configureViewResolvers(ViewResolverRegistry registry){
    }    
    
    등등...
}
```

기본 구현은 모두 비어있는 구현이다. 인터페이스를 상속하여 재정의가 필요한 메서드만 선별적으로 구현하면 된다.



## 5. JSP를 위한 ViewResolver



컨트롤러의 처리 결과를 JSP를 이용하여 생성하기 위해 다음과 같은 설정을 사용한다.

```java
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer{
    //...
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry){
        registry.jsp("/WEB-INF/view/",".jsp");
    }
}
```

WebMvcConfigurer 인터페이스에 정의된 configureViewResolvers() 메소드는 ViewResolverRegistry 타입의 **registry** 파라미터를갖는다.

ViewResolverRegistry.jsp()메소드를 사용하면 JSP를위한 ViewResolver를 설정할수있다.



위 설정은 o.s.w.servlet.view.InternalResourceViewResolver 클래스를 이용해 다음설정과 같은 빈을 등록한다.

```java
@Bean
public ViewResolver viewResolver(){
    InternalResourceViewResolver vr=new InternalResourceViewResolver();
    vr.setPrefix("/WEB-INF/view/");
    vr.setSuffix(".jsp");
    return vr;
}
```

컨트롤러의 실행 결과를 받은 DispatcherServlet은 ViewResolver에게 뷰이름에 해당하는 View객체를 요청한다.

이때 InternalResourceViewResolver는 "prefix+뷰이름+suffix"에 해당하는 경로를 뷰코드로 사용하는 InternalResourceView타입의 View객체를 리턴한다. 

예를들어 뷰이름이 "hello"라면 "/WEB-INF/view/hello.jsp"경로를 뷰코드로 사용하는 InternalResourceView 객체를 리턴한다.

DispatcherServlet이 InternalResourceView 객체에 응답생성을 요청하면 InternalResourceView 객체는 경로에 지정한 jsp코드를 실행해 응답결과를 생성한다.



----

DispatcherServlet은 컨트롤러의 실행 결과를 HandlerAdpater를 통해서 **ModelAndView** 형태로 받는다.

Model에 담긴값은 View객체에 Map형식으로 전달된다. 

예를들어 HelloController 클래스는 다음과같이 Model에 "greeting" 속성을 설정했다.

```java
@Controller
public class HelloController {
    
    @RequestMapping("/hello")
    public String hello(Model model, 
                        @RequestParam(value="name", required=false)String name) {
        model.addAttribute("greeting", "안녕하세요, " + name);
        return "hello";
    }
}
```

이 경우 DispatcherServlet은 View객체에 응답생성을 요청할때 greeting키를 갖는 Map객체를 model을 통하여 View객체에 전달하게 된다.

 

View객체는 전달받은 Map객체에 담긴 값을 이용해서 알맞은 응답결과를 출력한다. 

InternalResourceView는 Map객체에 담겨있는 key값을 request.setAttribute()를 이용해서 request의 속성에 저장한다. 그런뒤 해당 경로의 JSP를 실행한다.

결과적으로 컨트롤러에서 지정한 Model속성은 request객체 속성으로 JSP에 전달되기 때문에 JSP는 액션테스를 활용하여 다음과 같이 모델에 지정한 속성이름을 사용해서 값을 사용할수있게된다.

```xml
인사말:${greeting}
```

## 6. 디폴트 핸들러와 HandlerMapping 우선순위

앞 챕터의 web.xml 설정을 보면 DispatcherServlet에대한 매핑경로를 다음과 같이 '/'로 주었다.

```xml
<servlet>
    <servlet-name>dispatcher</servlet-name>
    <servlet-class>
        org.springframework.web.servlet.DispatcherServlet
    </servlet-class>
    ...생략
</servlet> 

<servlet-mapping>
    <servlet-name>dispatcher</servlet-name>
    <url-pattern>/</url-pattern>
</servlet-mapping>
```

매핑경로가 '/'인 경우 **.jsp**로 끝나는 요청을 제외한 모든 요청을 DispatcherServlet이 처리한다.
즉 /index.html이나 /css/bootstrap.css와 같이 확장자가 .jsp가 아닌 모든요청을 DispatcherServlet이 처리한다.



그런데 @EnableWebMvc애노테이션이 등록하는 HandlerMapping은 @Controller애노테이션을 적용한
빈객체가 처리할 수 있는 요청 경로만 대응할수있다. 

예를들어 등록된 컨트롤러가 한개이고, 그 컨트롤러가 @GetMapping("/hello")설정을 사용한다면 /hello경로만 처리할수있게된다.

따라서 "/index.html"이나 "/css/bootstrap.css"와 같은 요청을 처리할 수 있는 컨트롤러 객체를 찾지 못해 DispatcherServlet은 404응답을 전송한다.

"/index.html"이나 "/css/bootstrap.css"와 같은 경로를 처리하기 위한 컨트롤러 객체를
직접 구현할 수도있지만, 그보다는 WebMvcConfigurer의 configureDefaultServletHandling()메소드
를 사용하는것이 편하다. 



앞장에서도 다음설정을 사용했다.

```java
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer{
    @Override
    public void configureDefaultServletHandling(
        DefualtServletHandlerConfigurer configurer){
        configurer.enable();
    }
}
```

위 설정에서 DefaultServletHandlerConfigurer#enable() 메소드는 다음의 두빈객체를 추가한다.

- **DefaultServletHttpRequestHandler**
- **SimpleUrlHandlerMapping**



DefaultServletHttpRequestHandler는 클라이언트의 모든요청을 WAS(웹 어플리케이션 서버, 톰캣이나 웹로직 등)가 제공하는 디폴트 서블릿에 전달한다. 

예를들어 "/index.html"에 대한 처리를 DefaultServletHttpRequestHandler에 요청하면 이요청을 다시 디폴트 서블릿에 전달해서 처리하도록 한다.

그리고 SimpleUrlHandlerMapping을 이용해서 모든 경로("/**")를 DefaultServletHttp RequestHandler 를 이용해서 처리하도록 설정한다.

@EnableWebMvc 애노테이션이 등록하는 RequestMappingHandlerMapping의 적용 우선순위가
DefaultServletHandlerConfigurer.eanble()메소드가 등록하는SimpleUrlHandlerMapping의 우선순위 보다 높다. 

때문에 웹브라우저의 요청이 들어오면 DispatcherServlet은 다음과 같은방식으로 요청을 처리한다.



1) RequestMappingHandlerMapping을 사용해서 요청을 처리할 핸들러를 검색한다.

   - 존재하면 해당 컨트롤러를 이용해서 요청을 처리한다

     

2) 존재하지 않으면 SimpleUrlHandlerMapping을 사용해서 요청을 처리할 핸들러를 검색한다.

   

- DefaultServletHandlerConfigurer.eanble() 메소드가 등록한 SimpleUrlHandlerMapping은 "/**" 경로 

  (즉 모든경로)에 대해 DefaultServletHttpRequestHandler를 리턴한다.

- DispatcherServlet은 DefaultServletHttpRequestHadler에 처리를 요청한다.

- DefaultServletHttpRequestHandler는 디폴트 서블릿에 처리를 위임한다.

예를 들어 "index.html" 경로로 요청이 들어오면 1번과정에서 해당하는 컨트롤러를 찾지 못하므로 2번 과정을 통해 디폴트 서블릿이 /index.html요청을 처리하게된다.

DefaultServletHandlerConfigurer#enable()외에 몇몇 설정도 SimpleUrlHandlerMapping을 등록
하는데 DefualtServletHandlerConfiguer#enable()이 등록하는 SimpleUrlHandlerMapping의 우선
순위가 가장낮다. 따라서 DefaultServletHandlerConfigurer#eanble()을 설정하면 별도 설정이
없는 모든 요청경로를 디폴트 서블릿이처리하게된다.
