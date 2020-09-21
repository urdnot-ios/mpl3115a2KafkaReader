# iotKafkaMPL3115A2

This is a straightforward reader for one sensor, the MPL3115A2. 

I'm using [Circe's "Optics" libraries](https://circe.github.io/circe/optics.html) which converts
 the JSON into my Class structure. The data is then packaged up for Influx using a string builder function that I kinda hate but have to
use for Influx.

The bad news is that Circe's Optics uses Scala's ["Dynamic" feature](https://stackoverflow.com/questions/15799811/how-does-type-dynamic-work-and-how-to-use-it) 
which means it won't tell you that you've made a typo or missed a field. So test this carefully and 
thoroughly.
