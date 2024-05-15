package com.moyeo.dao;

import com.moyeo.vo.MaterialPhoto;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MaterialPhotoDao {
  void add(MaterialPhoto materialPhoto);

  int addAll(List<MaterialPhoto> materialPhotos);

  List<MaterialPhoto> findAllByMaterialId(int materialId);

  MaterialPhoto findByMaterialPhotoId(int materialPhotoId);

  int delete(int materialPhotoId);

  int deleteAll(int materialId);
}
