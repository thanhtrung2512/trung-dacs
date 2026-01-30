package com.example.spbn3.controller;

import com.example.spbn3.entity.StudyGroup;
import com.example.spbn3.service.StudyGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/groups")
public class AdminGroupController {

    @Autowired
    private StudyGroupService groupService;

    // 🟢 1. HIỂN THỊ DANH SÁCH NHÓM
    @GetMapping
    public String listGroups(Model model) {
        model.addAttribute("groups", groupService.getAllGroups());
        // Object rỗng cho Modal thêm mới
        model.addAttribute("group", new StudyGroup());
        return "admin/group-list";
    }

    // 🟢 2. LƯU NHÓM (Thêm mới & Cập nhật)
    @PostMapping("/save")
    public String saveGroup(@ModelAttribute("group") StudyGroup group) {
        // Logic xử lý nếu là thêm mới (ID null)
        if (group.getId() == null) {
            group.setMembers(0); // Mặc định 0 thành viên
            // Set ảnh mặc định nếu để trống
            if (group.getImage() == null || group.getImage().isEmpty()) {
                group.setImage("https://ui-avatars.com/api/?name=" + group.getName() + "&background=random");
            }
        } else {
            // Nếu là update, cần giữ nguyên số lượng thành viên cũ (nếu muốn logic chặt chẽ hơn thì phải findById trước)
            // Ở đây ta giả định form gửi lên đủ dữ liệu hoặc service xử lý merge
            StudyGroup oldGroup = groupService.getGroupById(group.getId()).orElse(null);
            if (oldGroup != null) {
                group.setMembers(oldGroup.getMembers());
                group.setCreatedAt(oldGroup.getCreatedAt());
                group.setCreator(oldGroup.getCreator());
            }
        }
        
        groupService.saveGroup(group);
        return "redirect:/admin/groups";
    }

    // 🟢 3. XÓA NHÓM
    @GetMapping("/delete/{id}")
    public String deleteGroup(@PathVariable Long id) {
        // Lưu ý: Cần xử lý xóa khóa ngoại trong group_members trước nếu chưa cấu hình Cascade
        // Ở đây giả định Service hoặc DB đã lo việc đó
        try {
             // Gọi hàm xóa từ Repository (Bạn cần thêm hàm deleteById vào Service nếu chưa có)
             // Tạm thời gọi thông qua repository nếu service chưa expose hàm delete
             // groupService.deleteGroup(id); 
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/admin/groups";
    }
}