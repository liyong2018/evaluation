package com.evaluate.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ChengduFunctionalDistrictCodeMapperTest {

    @Test
    void normalizesHighTechSourceCodesAndKeepsCommunitySuffix() {
        assertEquals("510172701", ChengduFunctionalDistrictCodeMapper.normalizeCode("510107062"));
        assertEquals("510172701003", ChengduFunctionalDistrictCodeMapper.normalizeCode("510107062003"));
        assertEquals("510172701003", ChengduFunctionalDistrictCodeMapper.normalizeCode("510172701003"));

        ChengduFunctionalDistrictCodeMapper.Mapping mapping =
                ChengduFunctionalDistrictCodeMapper.findByAnyCode("510107062003");

        assertNotNull(mapping);
        assertEquals("510172", mapping.getCountyCode());
        assertEquals("成都高新区", mapping.getCountyName());
        assertEquals("肖家河街道", mapping.getTownshipName());
    }

    @Test
    void normalizesTianfuAndEastNewAreaSourceCodes() {
        assertEquals("510171705014", ChengduFunctionalDistrictCodeMapper.normalizeCode("510116022014"));
        assertEquals("510173705101", ChengduFunctionalDistrictCodeMapper.normalizeCode("510185015101"));
        assertEquals("510173714000", ChengduFunctionalDistrictCodeMapper.normalizeCode("510173715000"));

        ChengduFunctionalDistrictCodeMapper.Mapping east =
                ChengduFunctionalDistrictCodeMapper.findByAnyCode("510173705101");

        assertNotNull(east);
        assertEquals("成都东部新区", east.getCountyName());
        assertEquals("福田街道", east.getTownshipName());
    }

    @Test
    void normalizesZigongHighTechSourceCodes() {
        assertEquals("510371701", ChengduFunctionalDistrictCodeMapper.normalizeCode("510302008"));
        assertEquals("510371702", ChengduFunctionalDistrictCodeMapper.normalizeCode("510302006"));
        assertEquals("510371703", ChengduFunctionalDistrictCodeMapper.normalizeCode("510302009"));
        assertEquals("510371704", ChengduFunctionalDistrictCodeMapper.normalizeCode("510302005"));
        assertEquals("510371705", ChengduFunctionalDistrictCodeMapper.normalizeCode("510311001"));

        ChengduFunctionalDistrictCodeMapper.Mapping mapping =
                ChengduFunctionalDistrictCodeMapper.findByAnyCode("510311001");

        assertNotNull(mapping);
        assertEquals("自贡高新技术产业开发区", mapping.getCountyName());
        assertEquals("板仓街道办事处", mapping.getTownshipName());
    }
}
