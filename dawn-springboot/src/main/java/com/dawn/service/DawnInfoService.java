package com.dawn.service;

import com.dawn.model.dto.AboutDTO;
import com.dawn.model.dto.DawnAdminInfoDTO;
import com.dawn.model.dto.DawnHomeInfoDTO;
import com.dawn.model.dto.WebsiteConfigDTO;
import com.dawn.model.vo.AboutVO;
import com.dawn.model.vo.WebsiteConfigVO;

public interface DawnInfoService {

    void report();

    DawnHomeInfoDTO getDawnHomeInfo();

    DawnAdminInfoDTO getDawnAdminInfo();

    void updateWebsiteConfig(WebsiteConfigVO websiteConfigVO);

    WebsiteConfigDTO getWebsiteConfig();

    void updateAbout(AboutVO aboutVO);

    AboutDTO getAbout();

}
