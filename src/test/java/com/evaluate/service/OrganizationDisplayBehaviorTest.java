package com.evaluate.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class OrganizationDisplayBehaviorTest {

    @Autowired
    private IOrganizationService organizationService;

    @Autowired
    private IGrassrootsOrganizationService grassrootsOrganizationService;

    @Test
    void parentSourceYearFollowsLatestChangedDescendant() {
        List<Map<String, Object>> tree = organizationService.getOrganizationTree(null, 3, 2025);

        Map<String, Object> province = findByCode(tree, "51");
        Map<String, Object> city = findByCode(tree, "5101");
        Map<String, Object> longquanyi = findByCode(tree, "510112");

        assertNotNull(province);
        assertNotNull(city);
        assertNotNull(longquanyi);
        assertEquals(2025, longquanyi.get("sourceYear"));
        assertEquals(2025, city.get("sourceYear"));
        assertEquals(2025, province.get("sourceYear"));
    }

    @Test
    void eastNewAreaGrassrootsUsesSystemXlsNames() {
        List<Map<String, Object>> tree = grassrootsOrganizationService.getTreeByCountyCode("510173", 2025);

        Map<String, Object> futian = findByCode(tree, "510173705");
        Map<String, Object> community = findByCode(tree, "510173705001");
        Map<String, Object> invalidFallback = findByCode(tree, "510173715");

        assertNotNull(futian);
        assertNotNull(community);
        assertEquals("福田街道", futian.get("name"));
        assertNotEquals("510173705001", community.get("name"));
        assertEquals(null, invalidFallback);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> findByCode(List<Map<String, Object>> nodes, String code) {
        if (nodes == null) {
            return null;
        }
        for (Map<String, Object> node : nodes) {
            if (Objects.equals(code, node.get("code"))) {
                return node;
            }
            Map<String, Object> child = findByCode((List<Map<String, Object>>) node.get("children"), code);
            if (child != null) {
                return child;
            }
        }
        return null;
    }
}
