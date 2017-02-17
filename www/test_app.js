var TestApp = function() {};

TestApp.prototype.createUser = function(success, fail) {
    cordova.exec(success, fail, "Vognition","createUser", []);
};

var ts = new TestApp();
module.exports = ts;