# Mutual Fund Excel Generator
Generates top 10 mutual funds in each category and prints in excel

Setup :
1. Java 21 - install from official website based on OS
2. Postman/insomnia
3. IDE - STS/Eclipse/IntelliJ

Running the springboot application
1. Clone the repository in your local using the command "git clone https://github.com/vaishsridh/MutualFundGenerator.git"
2. Open the repository in any IDE of your choice
3. Run the springboot application
4. Hit send and download "http://localhost:8080/download" on postman


TODO : 
Short term :
1. Implement caching preferably by storing data in redis DB and refresh it every 24h at 11 PM IST
2. Data quality checks for missing NAVs. Present calculation includes 0 NAV.
3. Write junits
4. Enhance security - API urls are exposed as constants

Medium term :
1. Work on frontend to leverage the option of subscribing and adding to mailing list
2. Explore multithreading
3. Automate to mail excel to mail ids
4. Implement Holiday Calendar to not calculate on holidays
5. Host it on any server supporting springboot applications

Long term :
1. Enhance data quality by giving leeway to standard deviation and returns
2. Data collection is manual. It can be automated by considering funds with inception date > 5 years.
3. Expand to international mutual funds
4. Exclude funds involved in front running or scam by crawling across the web - Optional

   
