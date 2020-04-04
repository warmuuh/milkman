var md5 = (new Hashes.MD5).hex;
var sha1 = (new Hashes.SHA1).hex;
var sha256 = (new Hashes.SHA256).hex;
var sha512 = (new Hashes.SHA512).hex;
var rmd160 = (new Hashes.RMD160).hex;

var base64 = function (str) {
    return java.util.Base64.encoder.encodeToString(str.bytes);
};