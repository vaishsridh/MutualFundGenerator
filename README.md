# MutualFundGenerator
Generates top 10 mutual funds in each category and prints in excel

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

Long term :
1. Enhance data quality by giving leeway to standard deviation and returns
2. Data collection is manual. It can be automated by considering funds with inception date > 5 years.
3. Expand to international mutual funds
4. Exclude funds involved in front running or scam by crawling across the web - Optional

   
