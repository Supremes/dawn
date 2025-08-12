package com.dawn.controller;

import com.dawn.annotation.OptLog;
import com.dawn.model.dto.AboutDTO;
import com.dawn.model.dto.DawnAdminInfoDTO;
import com.dawn.model.dto.DawnHomeInfoDTO;
import com.dawn.model.dto.WebsiteConfigDTO;
import com.dawn.enums.FilePathEnum;
import com.dawn.model.vo.ResultVO;
import com.dawn.service.DawnInfoService;
import com.dawn.strategy.context.UploadStrategyContext;
import com.dawn.model.vo.AboutVO;
import com.dawn.model.vo.WebsiteConfigVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

import static com.dawn.constant.OptTypeConstant.UPDATE;
import static com.dawn.constant.OptTypeConstant.UPLOAD;

@Api(tags = "dawn信息")
@RestController
public class DawnInfoController {

    @Autowired
    private DawnInfoService dawnInfoService;

    @Autowired
    private UploadStrategyContext uploadStrategyContext;

    @ApiOperation(value = "上报访客信息")
    @PostMapping("/report")
    public ResultVO<?> report() {
        dawnInfoService.report();
        return ResultVO.ok();
    }

    @ApiOperation(value = "获取系统信息")
    @GetMapping("/")
    public ResultVO<DawnHomeInfoDTO> getBlogHomeInfo() {
        return ResultVO.ok(dawnInfoService.getDawnHomeInfo());
    }

    @ApiOperation(value = "获取系统后台信息")
    @GetMapping("/admin")
    public ResultVO<DawnAdminInfoDTO> getBlogBackInfo() {
        return ResultVO.ok(dawnInfoService.getDawnAdminInfo());
    }

    @OptLog(optType = UPDATE)
    @ApiOperation(value = "更新网站配置")
    @PutMapping("/admin/website/config")
    public ResultVO<?> updateWebsiteConfig(@Valid @RequestBody WebsiteConfigVO websiteConfigVO) {
        dawnInfoService.updateWebsiteConfig(websiteConfigVO);
        return ResultVO.ok();
    }

    @ApiOperation(value = "获取网站配置")
    @GetMapping("/admin/website/config")
    public ResultVO<WebsiteConfigDTO> getWebsiteConfig() {
        return ResultVO.ok(dawnInfoService.getWebsiteConfig());
    }

    @ApiOperation(value = "查看关于我信息")
    @GetMapping("/about")
    public ResultVO<AboutDTO> getAbout() {
        return ResultVO.ok(dawnInfoService.getAbout());
    }

    @OptLog(optType = UPDATE)
    @ApiOperation(value = "修改关于我信息")
    @PutMapping("/admin/about")
    public ResultVO<?> updateAbout(@Valid @RequestBody AboutVO aboutVO) {
        dawnInfoService.updateAbout(aboutVO);
        return ResultVO.ok();
    }

    @OptLog(optType = UPLOAD)
    @ApiOperation(value = "上传博客配置图片")
    @ApiImplicitParam(name = "file", value = "图片", required = true, dataType = "MultipartFile")
    @PostMapping("/admin/config/images")
    public ResultVO<String> savePhotoAlbumCover(MultipartFile file) {
        return ResultVO.ok(uploadStrategyContext.executeUploadStrategy(file, FilePathEnum.CONFIG.getPath()));
    }

}
