package com.moyeo.service;

import com.moyeo.vo.ReviewComment;

public interface ReviewCommentService {

  void add(ReviewComment reviewComment);


  int delete(int reviewCommentId);

  int update(ReviewComment reviewComment);


}
