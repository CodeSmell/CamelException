# Camel Exception Spike 
This application is built as part of a spike to test how Camel handled exceptions on the route. The main goal is to assess how error handling and offset management work.

Revamped this older project upgrading from Camel 2.x and Java 8 to Camel 4.x and Java 17
- the older version is in the `master` branch
- the newer version is in the `main` branch

The old sample explored the following:
- handled vs unhandled exceptions
- exceptions that occur in the `onException`

This new sample now explores the following:s
- handled vs unhandled exceptions
- exceptions that occur in the `onException`
- exceptions that occur on a sub-route in another `RouteBuilder`
    - approaches to managing these exceptions 
    - using the `RouteConfigurationBuilder`
    - removing the `ErrorHandler`
       
        
