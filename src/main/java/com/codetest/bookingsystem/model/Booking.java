package com.codetest.bookingsystem.model;

import com.codetest.bookingsystem.enums.BookingStatus;
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
@Table(name = "booking")
public class Booking extends Auditable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "class_schedule_id", nullable = false)
	private ClassSchedule classSchedule;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_package_id", nullable = false)
	private UserPackage userPackage; // To know which package was used for booking

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private BookingStatus status;
}