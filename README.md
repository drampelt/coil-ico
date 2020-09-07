# coil-ico

A simple ICO decoder for [Coil](https://github.com/coil-kt/coil).

## Android ICO Support

ICO decoding is supported to a certain extent on most API levels, but this library makes it work more consistently across older versions.

|API Level|Support|
|---|---|
|14|Partial support for single BMP image|
|16|Single BMP image|
|19|Multiple PNG or BMP images, only the first image in the file will be used|
|24|Full support for multiple PNG or BMP images|


|API 16|API 21|
|---|---|
|![API 16](api16.png)|![API 21](api21.png)|
