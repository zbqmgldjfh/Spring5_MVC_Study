package config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import controller.RegisterController;
import spring.MemberRegisterService;
import survey.SurveyController;

@Configuration
public class ControllerConfig {
	
	@Autowired
	private MemberRegisterService memeberRegSvc;

	@Bean
	public RegisterController registerController() {
		RegisterController controller = new RegisterController();
		controller.setMemberRegisterService(memeberRegSvc);
		return controller;
	}
	
	@Bean
	public SurveyController surveyController() {
		return new SurveyController();
	}
}
