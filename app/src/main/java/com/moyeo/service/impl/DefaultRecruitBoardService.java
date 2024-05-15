package com.moyeo.service.impl;

import com.moyeo.dao.AlarmDao;
import com.moyeo.dao.MaterialDao;
import com.moyeo.dao.MessageDao;
import com.moyeo.dao.RecruitBoardDao;
import com.moyeo.dao.RecruitCommentDao;
import com.moyeo.dao.RecruitMemberDao;
import com.moyeo.dao.RecruitPhotoDao;
import com.moyeo.dao.RecruitScrapDao;
import com.moyeo.service.RecruitBoardService;
import com.moyeo.vo.Alarm;
import com.moyeo.vo.RecruitBoard;
import com.moyeo.vo.RecruitComment;
import com.moyeo.vo.RecruitPhoto;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class DefaultRecruitBoardService implements RecruitBoardService {

  private static final Log log = LogFactory.getLog(DefaultRecruitBoardService.class);
  private final RecruitBoardDao recruitBoardDao;
  private final RecruitScrapDao recruitScrapDao;
  private final RecruitCommentDao recruitCommentDao;
  private final RecruitPhotoDao recruitPhotoDao;
  private final RecruitMemberDao recruitMemberDao;
  private final MessageDao messageDao;
  private final AlarmDao alarmDao;
  private final MaterialDao materialDao;

  @Transactional
  @Override
  public void add(RecruitBoard board) {
    recruitBoardDao.add(board);
    if (board.getPhotos() != null && board.getPhotos().size() > 0) {
      for (RecruitPhoto photo : board.getPhotos()) {
        photo.setRecruitBoard(board);
      }
      recruitPhotoDao.addAll(board.getPhotos());
    }
  }

  @Override
  public List<RecruitBoard> list(int pageNo, int pageSize, int regionId, int themeId, String filter, String keyword) {
    return recruitBoardDao.findAll(pageSize * (pageNo - 1), pageSize, regionId, themeId, filter, keyword);
  }

  @Override
  public List<RecruitBoard> mypost(int pageNo, int pageSize, int memberId) {
    return recruitBoardDao.findByMemberId(pageSize * (pageNo - 1), pageSize, memberId);
  }

  @Override
  public List<RecruitBoard> myrequest(int pageNo, int pageSize, int memberId) {
    return recruitBoardDao.findReqByMemberId(pageSize * (pageNo - 1), pageSize, memberId);
  }

  @Override
  public List<RecruitBoard> teamlist(int memberId) {
    return recruitBoardDao.findMyTeamByMemberId(memberId);
  }

  @Override
  public RecruitBoard get(int boardId) {
    RecruitBoard recruitBoard = recruitBoardDao.findBy(boardId);
    recruitBoard.setComments(recruitCommentDao.findAllByRecruitBoardId(boardId));
    if (recruitBoard.getComments() != null && recruitBoard.getComments().size()>0 ){
      log.debug("photoId: "+ recruitBoard.getComments().getFirst().getMember().getPhoto());
    }
    recruitBoard.setPhotos(recruitPhotoDao.findAllByBoardId(boardId));

    return recruitBoard;
  }

  @Transactional
  @Override
  public int update(RecruitBoard board) {
    int count = recruitBoardDao.update(board);
    recruitPhotoDao.deleteAllPhotoByRecruitBoardId(board.getRecruitBoardId());

    if (board.getPhotos() != null && board.getPhotos().size() > 0) {
      for (RecruitPhoto recruitPhoto : board.getPhotos()) {
        recruitPhoto.setRecruitBoard(board);
      }
      recruitPhotoDao.addAll(board.getPhotos());
    }
    return count;
  }

  @Transactional
  @Override
  public int delete(int boardId) {
    recruitScrapDao.deleteAllScrapByRecruitBoardId(boardId);
    recruitCommentDao.deleteAllCommentByRecruitBoardId(boardId);
    recruitPhotoDao.deleteAllPhotoByRecruitBoardId(boardId);
    materialDao.deleteAllByRecruitBoardId(boardId);
    messageDao.delete(boardId); // 해당 모집글의 채팅방의 채팅 삭제
    recruitMemberDao.deleteAll(boardId); // recruit_member 테이블의 recruitBoardId가 boardId인 레코드 전부 삭제

    List<Alarm> alarmList = alarmDao.listAll();
    // 관련 없는 알림을 제거해야 하는 리스트
    List<Alarm> removeAlarm = new ArrayList<>();
    // 관련 알림 내용
    String removeStr = boardId + "번 모집";
    // 관련 없는 알림 리스트
    for (Alarm alarm : alarmList) {
      if (!alarm.getContent().contains(removeStr)) {
        removeAlarm.add(alarm);
      }
    }
    // 관련 없는 알림 리스트를 제거
    alarmList.removeAll(removeAlarm);
    // 후기 삭제 할 때 삭제 해야하는 알림 제거
    for (Alarm alarm : alarmList) {
      alarmDao.delete(alarm.getAlarmId());
    }

    return recruitBoardDao.delete(boardId);
  }

  @Override
  public List<RecruitPhoto> getRecruitPhotos(int recruitBoardId) {
    return recruitPhotoDao.findAllByBoardId(recruitBoardId);
  }

  @Override
  public RecruitPhoto getRecruitPhoto(int recruitPhotoId) {
    return recruitPhotoDao.findById(recruitPhotoId);
  }

  @Override
  public int deleteRecruitPhoto(int recruitPhotoId) {
    return recruitPhotoDao.delete(recruitPhotoId);
  }

  @Override
  public void addComment(RecruitComment comment) {
    recruitCommentDao.add(comment);
  }

  @Override
  public RecruitComment getComment(int commentId) {
    return recruitCommentDao.findBy(commentId);
  }

  @Override
  public int updateComment(RecruitComment recruitComment) {
    return recruitCommentDao.update(recruitComment);
  }

  @Override
  public int deleteComment(int commentId) {
    return recruitCommentDao.delete(commentId);
  }

  public int countAll(int regionId, int themeId, String filter, String keyword) {
    return recruitBoardDao.countAll(regionId, themeId, filter, keyword);
  }

  public int countAllMyPost(@Param("memberId") int memberId){
    return recruitBoardDao.countAllMyPost(memberId);
  }

  public int countAllMyReq(@Param("memberId") int memberId){
    return recruitBoardDao.countAllMyReq(memberId);
  }

  @Override
  public void plusViews(int boardId) { // 조회수 증가
    recruitBoardDao.plusViews(boardId);
  }

  @Override
  public List<RecruitBoard> findByCurrent(int pageNo, int pageSize, int regionId, int themeId, String filter, String keyword) {
    return recruitBoardDao.findByCurrent(pageSize * (pageNo - 1), pageSize, regionId, themeId, filter, keyword);
  }

  @Override
  public List<RecruitBoard> findByCurrentByLimit6() {
    return recruitBoardDao.findByCurrentByLimit6();
  }

  @Override
  public RecruitBoard findCurrentAndTotalBy(int recruitBoardId) { // 현재 인원과 총 모집 인원을 찾는 메서드
    return recruitBoardDao.findCurrentAndTotalBy(recruitBoardId);
  }
}

