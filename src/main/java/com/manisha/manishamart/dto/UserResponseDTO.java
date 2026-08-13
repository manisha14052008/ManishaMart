package com.manisha.manishamart.dto;

import com.manisha.manishamart.model.User;
import java.time.LocalDateTime;

public class UserResponseDTO {
    private long id;
    private String name;
    private String email;
    private String role;
    private LocalDateTime createdAt;

    public UserResponseDTO() {}

    public static UserResponseDTO fromUser(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.id = user.getId();
        dto.name = user.getName();
        dto.email = user.getEmail();
        dto.role = user.getRole().name();
        dto.createdAt = user.getCreatedAt();
        return dto;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
