package com.dawn.service;

import com.dawn.model.dto.PageResultDTO;
import com.dawn.model.dto.PhotoAdminDTO;
import com.dawn.model.dto.PhotoAlbumAdminDTO;
import com.dawn.model.dto.PhotoDTO;
import com.dawn.entity.Photo;
import com.dawn.model.vo.*;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PhotoService extends IService<Photo> {

    PageResultDTO<PhotoAdminDTO> listPhotos(ConditionVO conditionVO);

    void updatePhoto(PhotoInfoVO photoInfoVO);

    void savePhotos(PhotoVO photoVO);

    void updatePhotosAlbum(PhotoVO photoVO);

    void updatePhotoDelete(DeleteVO deleteVO);

    void deletePhotos(List<Integer> photoIds);

    PhotoDTO listPhotosByAlbumId(Integer albumId);

}
