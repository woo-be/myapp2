package com.moyeo.controller;

import com.moyeo.service.AlarmService;
import com.moyeo.vo.Alarm;
import com.moyeo.vo.Member;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("alarm")
@Controller
@RequiredArgsConstructor
public class AlarmController {

  private final static Log log = LogFactory.getLog(AlarmController.class);
  private final AlarmService alarmService;

  @GetMapping("list")
  @ResponseBody
  public List<Alarm> list(HttpSession session) {
    Member loginUser = (Member) session.getAttribute("loginUser");
    List<Alarm> list = new ArrayList<Alarm>();
    list = alarmService.list(loginUser.getMemberId());
    return list;
  }

  @GetMapping("delete")
  @ResponseBody
  public void delete(int alarmId){
    alarmService.delete(alarmId);
  }
}
