# SWIM-NLNG
Proof-of-concept Natural Language Narrative Generator service for the SWIM platform.

## Description
The SWIM-NLNG is a webservice component that forms part of the SWIM platform (https://water.cybershare.utep.edu/swim).
Its main function is to generate model element narratives on request. NLNG retrieves narrative components from SWIM's
database for processing. SWIM's database is currently managed with an instance of MongoDB.

## Environment
+ Java JDK 1.8
+ Apache Tomcat/8.0.0-RC1
+ Cross Platform

## Data Dependencies
+ MongoDB
  - SWIM narrative template collection.
  - SWIM user scenario collection. 
  
## Demo
Visit: http://purl.org/swim/services/nlng
  
## Contributors
+ Alejandro Vargas - ravargasaco@miners.utep.edu
+ Luis Garnica - luis@gitgudconsulting.com

## Sponsor
This material is based upon work supported by the United States Department of Agriculture under Grant No. 2015-68007-23130

## Institution
+ University of Texas at El Paso
