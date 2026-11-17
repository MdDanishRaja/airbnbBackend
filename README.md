Airbnb Backend – Hotel Discovery & Booking

A Spring Boot REST backend for a hotel discovery and booking platform. Guests can search hotels by city and dates, book rooms and pay online; hotel managers can manage hotels, rooms, inventory and view reports.

Built while following the Coding Shuttle Spring Boot course (Airbnb clone).

Features
Authentication & authorization – sign up / login with JWT access tokens and an HttpOnly refresh-token cookie; role-based access (GUEST, HOTEL_MANAGER).
Hotel & room management (managers) – create, update, activate and delete hotels and rooms; a year of date-wise inventory is generated for every room.
Hotel search – paginated search by city, date range and number of rooms, based on available inventory and the minimum price.
Booking workflow – RESERVED → GUESTS_ADDED → PAYMENTS_PENDING → CONFIRMED / CANCELLED. A booking must be completed within 10 minutes of creation.
No double booking – inventory rows are locked with pessimistic write locks (@Lock(PESSIMISTIC_WRITE)) while a room is reserved, confirmed or cancelled.
Payments – Stripe Checkout sessions, a signature-verified webhook to confirm payments, and automatic refunds on cancellation.
Dynamic pricing – prices are built with the Decorator pattern (base price, surge factor, occupancy, urgency, holiday). A scheduled job refreshes inventory prices and the hotel minimum prices every hour.
Reports – confirmed bookings, total revenue and average revenue per hotel for a date range.
API docs – Swagger / OpenAPI UI via springdoc.
Tech stack

Java, Spring Boot, Spring Data JPA, Hibernate, PostgreSQL, Spring Security, JWT (jjwt), Stripe Java SDK, ModelMapper, Lombok, springdoc-openapi, Maven, JUnit 5, Mockito.

Project structure
airBnbApp/src/main/java/com/codingshuttle/projects/airBnbApp
├── advice        global exception handler and common API response wrapper
├── config        Stripe and ModelMapper configuration
├── controller    REST controllers
├── dto           request / response objects
├── entity        JPA entities and enums
├── exception     custom exceptions
├── repository    Spring Data JPA repositories (with locking queries)
├── security      JWT filter, JWT service, security configuration
├── service       business logic (booking, inventory, hotel, user, checkout, price updates)
├── strategy      dynamic pricing strategies (Decorator pattern)
└── util          helpers
Booking flow
POST /bookings/init – checks availability for every night, locks the inventory rows, reserves the rooms and creates a RESERVED booking with the total price.
POST /bookings/{id}/addGuests – attaches guests (GUESTS_ADDED).
POST /bookings/{id}/payments – creates a Stripe Checkout session and returns its URL (PAYMENTS_PENDING).
Stripe calls POST /webhook/payment – after the signature check the booking becomes CONFIRMED and the reserved rooms are marked as booked.
POST /bookings/{id}/cancel – only for CONFIRMED bookings; releases the inventory and refunds the payment through Stripe.
Dynamic pricing

PricingService wraps a BasePricingStrategy with these decorators:

Strategy	Effect
Surge	multiplies the price by the inventory's surge factor (set by the manager)
Occupancy	+20% when more than 80% of the rooms are booked
Urgency	+15% for dates in the next 7 days
Holiday	+25% (currently a placeholder; a real holiday check is still to be added)
API overview

Swagger UI: http://localhost:8080/swagger-ui/index.html

Area	Endpoints
Auth	POST /auth/signup, POST /auth/login, POST /auth/refresh
Browse hotels	GET /hotels/search (JSON body: city, startDate, endDate, roomsCount, page, size), GET /hotels/{hotelId}/info
Bookings (login required)	POST /bookings/init, POST /bookings/{id}/addGuests, POST /bookings/{id}/payments, POST /bookings/{id}/cancel, GET /bookings/{id}/status
Users (login required)	GET/PATCH /users/profile, GET /users/myBookings, GET/POST /users/guests, PUT/DELETE /users/guests/{guestId}
Hotel manager	/admin/hotels (CRUD, PATCH /{id}/activate, GET /{id}/bookings, GET /{id}/reports), /admin/hotels/{hotelId}/rooms (CRUD), GET/PATCH /admin/inventory/rooms/{roomId}
Webhook	POST /webhook/payment (Stripe)

Send the access token as Authorization: Bearer <token>. /admin/** needs the HOTEL_MANAGER role; new users are created with the GUEST role.

Getting started

Prerequisites: JDK 25 (see java.version in pom.xml), Maven (or the included mvnw), PostgreSQL, a Stripe account in test mode.

Create a PostgreSQL database (for example airbnb).
Copy airBnbApp/src/main/resources/application.properties.example to application.properties in the same folder and fill in the database, JWT and Stripe values. This file is git-ignored, so secrets are never committed.
Run the app:
bash
cd airBnbApp
./mvnw spring-boot:run
For payments locally, forward Stripe webhooks with the Stripe CLI:
bash
stripe listen --forward-to localhost:8080/webhook/payment

and put the printed signing secret in stripe.webhook.secret.
