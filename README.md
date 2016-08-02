# BookRental
BookRental is one of my first **Java EE** projects based on **JSF/JPA/JDB**. It is a web application with database that allows managing book rentals from the perspective of user, worker and administrator. Thinking back over, some parts could have been written better (like model logic). However, it was a good learning experience. Detailed overview can be found on [the project page](https://t3r1jj.github.io/BookRental "BookRental project page").

## Installation
To install BookRental:
1. Create JDB (Java DB) with default settings (*jdbc:derby://localhost:1527/library*):
  + db name: *library*,
  + username: *app*,
  + password: *app*.  
  See glassfish-resources.xml for more details.
2. Deploy on server (preferably GlassFish Application Server 3.1 or newer).

## Gallery [[more]](https://t3r1jj.github.io/BookRental "BookRental project page")
<p align="center">
<b>Main page</b><br/>
<img src="https://cloud.githubusercontent.com/assets/20327242/21473010/25abe762-caf5-11e6-9f69-e9fe56b8456f.png" alt="BookRental main page screenshot">

## License
Copyright 2016 Damian Terlecki

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
