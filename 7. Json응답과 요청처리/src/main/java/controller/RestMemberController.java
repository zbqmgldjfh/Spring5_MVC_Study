package controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import spring.DuplicateMemberException;
import spring.Member;
import spring.MemberDao;
import spring.MemberRegisterService;
import spring.RegisterRequest;

@RestController
public class RestMemberController {
	
	private MemberDao memberDao;
	private MemberRegisterService registerService;
	
	public RestMemberController(MemberDao memberDao, MemberRegisterService registerService) {
		this.memberDao = memberDao;
		this.registerService = registerService;
	}
	
	@GetMapping("/api/members")
	public List<Member> members(){
		return memberDao.selectAll();
	}

	@GetMapping("/api/members/{id}")
	public ResponseEntity<Object> member(@PathVariable Long id, HttpServletResponse response) throws IOException {
		Member member = memberDao.selectById(id);
		if(member == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("no member"));
		}
		return ResponseEntity.status(HttpStatus.OK).body(member);
	}
	
	@PostMapping("/api/members")
	public void newMember(@RequestBody @Valid RegisterRequest regReq, HttpServletResponse response) throws IOException{
		try {
			Long newMemberId = registerService.regist(regReq);
			response.setHeader("Location", "/api/members" + newMemberId);
			response.setStatus(HttpServletResponse.SC_CREATED);
		}catch (DuplicateMemberException dupEx) {
			response.sendError(HttpServletResponse.SC_CONFLICT);
		}
	}
}
