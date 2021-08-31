const functions = require("firebase-functions");

 const admin = require("firebase-admin");
 admin.initializeApp();

var fs = admin.firestore();
var db = admin.database();
//var userId;

/*firebase.auth().currentUser.getIdToken(true)
.then(function (token) {
    // You got the user token
    userId = token;
})
.catch(function (err) {
    console.error(err);
});*/

/*firebase.auth().onAuthStateChanged((user) => {
  if (user) {
    // User logged in already or has just logged in.
    //console.log(user.uid);

    userId = user.uid;
  } else {
    // User not logged in or has just logged out.
  }
});*/

/*experts.getUserId = functions.https.onRequest(async (request, response) => {
    const userEmail = request.body.userEmail;

    admin.auth.getUserById(userEmail)
        .then(userRecord => {
            const uid = userRecord.uid
    })
})*/


exports.scheduledFunction = functions.pubsub.schedule("0 15 * * *").timeZone('UTC+8').onRun((context) => {
    /*db.once('value').then(snapshot => {
        snapshot.forEach(function (child){
            db.ref("Attendance/" + child.key + "/status").set("ok");
            //child.key
        });
    });

    db.ref("Attendance/123/status").set("888");*/

    //const ref = db.ref('Attendance');

    db.ref("/").once('value', function(snapshot){
        snapshot.forEach(function(childSnapshot){
            var childKey = childSnapshot.key;

            console.log("childKey: " + childKey);

            db.ref("Attendance/" + childSnapshot.key + "/status").set(childSnapshot.key);

            //const ref = db.ref('Attendance');
            //set(ref(db, 'users/' + childKey), {
                //username: "name",
                //email: "email",
                //profile_picture : "imageUrl"
            //  });

            //const attendanceRef = db.child(childKey).child("Attendance");
            //attendanceRef.once('value', function(snapshot2){
            //    if(snapshot2.hasChild()){
            //        attendanceRef.update("clockOutDate", "ytdDate");
            //    }
            //});
        });
    });

    /*db.once('value', function(snapshot){
        snapshot.forEach(function(childSnapshot){
            var childKey = childSnapshot.key;

            console.log("childKey: " + childKey);

            //const ref = db.ref('Attendance');
            //set(ref(db, 'users/' + childKey), {
                //username: "name",
                //email: "email",
                //profile_picture : "imageUrl"
            //  });

            //const attendanceRef = db.child(childKey).child("Attendance");
            //attendanceRef.once('value', function(snapshot2){
            //    if(snapshot2.hasChild()){
            //        attendanceRef.update("clockOutDate", "ytdDate");
            //    }
            //});
        });
    });*/

 //fs.doc("temp/0125558521").update({"timestamp" : admin.firestore.Timestamp.now() + " and " + userId});

   return console.log("Successfully Update" + admin.firestore.Timestamp.now());
 });

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
/*exports.helloWorld = functions.https.onRequest((request, response) => {
   functions.logger.info("Hello logs!", {structuredData: true});
   response.send("Hello from Firebase!");
 });*/
