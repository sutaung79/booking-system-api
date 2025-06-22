package com.codetest.bookingsystem.model;

import com.codetest.bookingsystem.enums.WaitlistStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "waitlist")
public class Waitlist extends Auditable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "class_schedule_id", nullable = false)
	private ClassSchedule classSchedule;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private WaitlistStatus status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_package_id", nullable = false)
	private UserPackage userPackage; // To know which package was used to deduct credits
}