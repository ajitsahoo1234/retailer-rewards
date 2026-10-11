# **Retailer Rewards API**

Java 17 / Spring Boot 3.5.16 / Maven / Spring JDBC / H2 / JUnit 5 / Mockito.

# **Business rules**

Reward points are calculated separately for each purchase:

- **$50 or less:** No reward points.
- **More than $50 and up to $100:** Earn 1 point for every whole dollar above $50.
- **More than $100:** Earn 50 points for the portion between $50 and $100, plus 2 points for every whole dollar above $100.

### Examples

| Purchase Amount | Calculation | Reward Points |
|----------------:|-------------|--------------:|
| $50             | No points   | 0             |
| $80             | 80 − 50     | 30            |
| $100            | 100 − 50    | 50            |
| $120            | 50 + (20 × 2) | 90          |
| $150            | 50 + (50 × 2) | 150         |


#  ****Run in IntelliJ or a terminal****

Install JDK 17 and Maven 3.6.3 or newer. Open the folder containing pom.xml as a Maven project in IntelliJ, select JDK 17, and allow dependencies to download. Run RewardsApplication, or:

mvn clean verify
mvn spring-boot:run

The service listens on http://localhost:8080. There is no Maven wrapper; Maven must be installed or provided by your IDE. Initial dependency downloads require internet access.

To build and run the executable JAR:

mvn clean package
java -jar target/retailer-rewards-1.0.0.jar

# **REST endpoints**

Method	            URL	                                                Result
GET	            /api/rewards?startMonth=2026-01	                All customers, January through March
GET	            /api/rewards/101?startMonth=2026-01         	One customer, January through March
GET	            /api/rewards/101	                            Last three months ending with the latest stored transaction month
startMonth is optional and must be YYYY-MM with year 0001–9998. The window is always three consecutive calendar months. Queries use [first day of start month, first day of fourth month); year rollover works automatically. Months with no purchases appear with zero points. Existing customers with no purchases are included. If the purchase table is empty, the default ends in the current server calendar month. Unknown customer: 404; invalid ID/month: 400; unexpected failure: generic 500 with server-side logging.

curl "http://localhost:8080/api/rewards/101?startMonth=2026-01"

{
"customerId": 101,
"customerName": "Amit",
"monthlyRewards": {"2026-01": 120, "2026-02": 120, "2026-03": 120},
"totalRewards": 360
}

Postman: select GET, paste the URL, and click Send. No request body or authentication is required for this assignment.

# **Dataset and expected results**

72 purchases, 8 customers, November 2025–April 2026. DATASET.md lists every purchase and its independently calculated points. The extra months demonstrate date filtering and year rollover; each API report still covers exactly three months.

| ID  | Customer     | Jan 2026 | Feb 2026 | Mar 2026 | Total |
|-----|--------------|---------:|---------:|---------:|------:|
| 101 | Amit         |      120 |      120 |      120 |   360 |
| 102 | Rahul        |      190 |      190 |      190 |   570 |
| 103 | Priya        |      275 |      275 |      275 |   825 |
| 104 | Neha         |       50 |        0 |       50 |   100 |
| 105 | Vikram       |       50 |       50 |       50 |   150 |
| 106 | Sara         |     2248 |     2248 |     2248 |  6744 |
| 107 | John         |        0 |        0 |        0 |     0 |
| 108 | No Purchases |        0 |        0 |        0 |     0 |
