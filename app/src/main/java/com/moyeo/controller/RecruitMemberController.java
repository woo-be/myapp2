package com.moyeo.controller;

import com.moyeo.service.AlarmService;
import com.moyeo.service.RecruitBoardService;
import com.moyeo.service.RecruitMemberService;
import com.moyeo.vo.Alarm;
import com.moyeo.vo.ErrorName;
import com.moyeo.vo.Member;
import com.moyeo.vo.MoyeoError;
import com.moyeo.vo.RecruitBoard;
import com.moyeo.vo.RecruitMember;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("/recruitMember")
public class RecruitMemberController {

  private static final Log log = LogFactory.getLog(RecruitMemberController.class);
  private final RecruitMemberService recruitMemberService;
  private final RecruitBoardService recruitBoardService;
  private final AlarmService alarmService;

  @GetMapping("add")
  public String add(int recruitBoardId, HttpSession session) throws Exception { // 모집 신청하기

    Member loginUser = (Member) session.getAttribute("loginUser");
    if (loginUser == null) {
      throw new MoyeoError(ErrorName.LOGIN_REQUIRED, "/auth/form");
    }

    RecruitBoard recruitBoard = recruitBoardService.get(recruitBoardId);
    if (loginUser.getMemberId() == recruitBoard.getWriter().getMemberId()) {
      throw new MoyeoError(ErrorName.REJECTED_RECRUIT_MYPOST,
          "/recruit/view?recruitBoardId=" + recruitBoardId);
    }

    recruitMemberService.add(recruitBoardId, loginUser.getMemberId());

    // 알림 읽음여부를 확인하기 위해 알림 id를 같이 넘겨준다
    Alarm alarm = Alarm.builder().memberId(recruitBoard.getWriter().getMemberId()).content(
        "<a href=\"/myrecruit/appl?recruitBoardId=" +
            recruitBoardId).build();
    alarmService.recruitMemberAdd(alarm, recruitBoardId);

    return "redirect:/recruit/view?recruitBoardId=" + recruitBoardId;
  }

  @GetMapping("list")
  public void list(
      Model model,
      HttpSession session
  ) throws Exception {

    Member loginUser = (Member) session.getAttribute("loginUser");
    if (loginUser == null) {
      throw new MoyeoError(ErrorName.LOGIN_REQUIRED, "/auth/form");
    }

    List<RecruitBoard> list = recruitMemberService.list(loginUser.getMemberId());

    model.addAttribute("list", list);
    model.addAttribute("memberId", loginUser.getMemberId());
  }

  @GetMapping("test")
  public void test() {

  }

  @GetMapping("delete")
  public String delete(int recruitBoardId, HttpSession session) throws Exception { // 모집 신청 취소하기

    Member loginUser = (Member) session.getAttribute("loginUser");
    if (loginUser == null) {
      throw new MoyeoError(ErrorName.LOGIN_REQUIRED, "/auth/form");
    }

    recruitMemberService.delete(recruitBoardId, loginUser.getMemberId());

    return "redirect:/recruit/view?recruitBoardId=" + recruitBoardId;
  }

  @GetMapping("calendar")
  public void calendar(int recruitBoardId,
      Model model,
      HttpSession session
  ) throws Exception {

    Member loginUser = (Member) session.getAttribute("loginUser");
    if (loginUser == null) {
      throw new MoyeoError(ErrorName.LOGIN_REQUIRED, "/auth/form");
    }

    RecruitBoard team = recruitBoardService.get(recruitBoardId);
    List<RecruitMember> recruitMembers = recruitMemberService.allApplicant(recruitBoardId);

    for (int i = 0; i < recruitMembers.size(); i++) {
      log.debug(recruitMembers.get(i).getMemberId() + "@@@" + loginUser.getMemberId());
      if (recruitMembers.get(i).getMemberId() == loginUser.getMemberId()) {
        break;
      } else if (recruitMembers.size() - 1 == i) {
        throw new MoyeoError("권한이 없습니다.", "/home");
      }
    }

    model.addAttribute("team", team);
    model.addAttribute("nickname", loginUser.getNickname());
    model.addAttribute("recruitBoardId", recruitBoardId);
  }

  @GetMapping("planBoardList")
  public void planBoardList(String date, Model model) {
    model.addAttribute("date", date);
  }
}

