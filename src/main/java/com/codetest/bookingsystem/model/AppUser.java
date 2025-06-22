package com.codetest.bookingsystem.model;

import com.codetest.bookingsystem.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet; // Add this import
import java.util.Set; // Keep this import

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_user") // "user" is a reserved keyword in many SQL dialects
public class AppUser extends Auditable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String username;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false, unique = true)
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	private boolean enabled = false; // For email verification

	@OneToMany(mappedBy = "user")
	private Set<UserPackage> userPackages = new HashSet<>();
}