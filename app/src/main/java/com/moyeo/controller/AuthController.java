package com.moyeo.controller;

import com.moyeo.security.MemberUserDetails;
import com.moyeo.service.MemberService;
import com.moyeo.vo.Member;
import com.moyeo.vo.MoyeoError;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("/auth")
public class AuthController {

  private static final Log log = LogFactory.getLog(AuthController.class);
  private final MemberService memberService;

  // templates의 auth파일에서 form.html 에서 폼을 가져온다.
  @GetMapping("form")
  public void form(@CookieValue(required = false) String email, Model model){
    model.addAttribute("email", email);
  }

  // templates의 auth파일에서 login.html 에서 로그인성공 폼을 가져온다.
  @PostMapping("loginSuccess")
  public String login(
      String email,
      @AuthenticationPrincipal MemberUserDetails principal,
      String saveEmail,
      HttpServletResponse response,
      HttpSession session) throws MoyeoError {

    log.debug("로그인 성공!!!");

    log.debug(saveEmail);
    log.debug(principal);

    if (saveEmail != null) {
      Cookie cookie = new Cookie("email", principal.getUsername());
      cookie.setMaxAge(60 * 60 * 24 * 7);
      response.addCookie(cookie);
    } else {
      Cookie cookie = new Cookie("email", "");
      cookie.setMaxAge(0);
      response.addCookie(cookie);
    }

    Member member = memberService.get(email);
    if (member != null) {
      session.setAttribute("loginUser", principal.getMember());
      session.setAttribute("loginedMemberId", member.getMemberId());
      log.debug(String.format("컨텐트======================================\n%s", member.getIntroduce()));

    } else {
      // 로그인 실패 시 로그인 페이지로 다시 이동
      // 구현후 로그인이 실패되었습니다 메세지 나오게 설정하기!!
      throw new MoyeoError("아이디 또는 비밀번호가 일치하지 않습니다.","/auth/form");
    }

    // 로그인 성공 시 home.html로 리디렉션
    return "redirect:/home";
  }

  @GetMapping("/logout")
  public String logout(HttpSession session, HttpServletResponse response) {
    // 세션을 무효화하여 로그아웃
    session.invalidate();
    // 쿠키를 삭제하기 위해 빈 문자열과 유효 시간을 0으로 설정
    // 아이디 저장 나오게 하고싶으면 이부분 삭제
    Cookie cookie = new Cookie("email", "");
    cookie.setMaxAge(0);
    response.addCookie(cookie);
    // 로그아웃 후 리다이렉트
    return "redirect:/home";
  }

}
