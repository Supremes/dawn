package com.dawn.service;

import com.dawn.model.dto.PhotoAlbumAdminDTO;
import com.dawn.model.dto.PhotoAlbumDTO;
import com.dawn.entity.PhotoAlbum;
import com.dawn.model.vo.ConditionVO;
import com.dawn.model.dto.PageResultDTO;
import com.dawn.model.vo.PhotoAlbumVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PhotoAlbumService extends IService<PhotoAlbum> {

    void saveOrUpdatePhotoAlbum(PhotoAlbumVO photoAlbumVO);

    PageResultDTO<PhotoAlbumAdminDTO> listPhotoAlbumsAdmin(ConditionVO condition);

    List<PhotoAlbumDTO> listPhotoAlbumInfosAdmin();

    PhotoAlbumAdminDTO getPhotoAlbumByIdAdmin(Integer albumId);

    void deletePhotoAlbumById(Integer albumId);

    List<PhotoAlbumDTO> listPhotoAlbums();

}
