package com.evaluate.controller;

import com.evaluate.common.Result;
import com.evaluate.entity.Organization;
import com.evaluate.entity.OrganizationBoundary;
import com.evaluate.service.IOrganizationService;
import com.evaluate.service.OrganizationBoundaryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/organization/boundary")
public class OrganizationBoundaryController {

    @Autowired
    private OrganizationBoundaryService organizationBoundaryService;

    @Autowired
    private IOrganizationService organizationService;

    @GetMapping("/list/{orgId}")
    public Result<List<OrganizationBoundary>> getBoundaries(@PathVariable Long orgId) {
        return Result.success(organizationBoundaryService.getBoundariesByOrgId(orgId));
    }

    @GetMapping("/{orgId}/{year}")
    public Result<OrganizationBoundary> getBoundary(@PathVariable Long orgId, @PathVariable Integer year) {
        return Result.success(organizationBoundaryService.getBoundaryByOrgIdAndYear(orgId, year));
    }

    @PostMapping("/save")
    public Result<Boolean> saveBoundary(@RequestBody OrganizationBoundary boundary) {
        return Result.success(organizationBoundaryService.saveOrUpdateBoundary(boundary));
    }

    @PostMapping("/sync-defaults")
    public Result<String> syncDefaultBoundaries() {
        try {
            String projectPath = System.getProperty("user.dir");
            File boundariesDir = new File(projectPath, "frontend/public/boundaries");
            
            if (!boundariesDir.exists()) {
                return Result.error("边界数据目录不存在");
            }

            int count = 0;
            // Iterate over year directories (e.g., 2024, 2025)
            File[] yearDirs = boundariesDir.listFiles(File::isDirectory);
            if (yearDirs != null) {
                for (File yearDir : yearDirs) {
                    if (!yearDir.getName().matches("\\d{4}")) {
                        continue;
                    }
                    Integer year = Integer.parseInt(yearDir.getName());
                    
                    // Process city directory
                    File cityDir = new File(yearDir, "city");
                    if (cityDir.exists() && cityDir.isDirectory()) {
                        File[] cityFiles = cityDir.listFiles((dir, name) -> name.endsWith(".json"));
                        if (cityFiles != null) {
                            for (File cityFile : cityFiles) {
                                String orgName = cityFile.getName().replace(".json", "");
                                
                                // Find organization
                                Organization org = organizationService.getOne(new QueryWrapper<Organization>().eq("name", orgName));
                                if (org != null) {
                                    // Read content
                                    String content = new String(java.nio.file.Files.readAllBytes(cityFile.toPath()), java.nio.charset.StandardCharsets.UTF_8);
                                    
                                    // Create or update boundary
                                    OrganizationBoundary boundary = new OrganizationBoundary();
                                    boundary.setOrganizationId(org.getId());
                                    boundary.setYear(year);
                                    boundary.setBoundaryCoordinates(content);
                                    // Optionally set file path if needed, but currently reserved for tender docs
                                    // boundary.setFilePath("/boundaries/" + year + "/city/" + cityFile.getName());
                                    
                                    organizationBoundaryService.saveOrUpdateBoundary(boundary);
                                    count++;
                                }
                            }
                        }
                    }
                }
            }
            return Result.success("同步完成，共处理 " + count + " 条数据");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("同步失败: " + e.getMessage());
        }
    }

    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }
        try {
            // Determine upload directory
            String projectPath = System.getProperty("user.dir");
            // Save to frontend/public/uploads/boundary_files so it's accessible via frontend dev server
            File uploadDir = new File(projectPath, "frontend/public/uploads/boundary_files");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            File dest = new File(uploadDir, newFilename);
            file.transferTo(dest);

            // Return URL path (relative to public)
            return Result.success("/uploads/boundary_files/" + newFilename);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
}
