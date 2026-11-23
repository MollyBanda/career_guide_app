/**
 * One-time Node.js script to assign custom claim { admin: true }
 * to administrator UID: 9FqaKqOScRco4qv4tpNqtqd08Sf1 (mphasamolly@gmail.com)
 *
 * Usage Instructions:
 * 1. Download service account key JSON from Firebase Console:
 *    Project Settings -> Service accounts -> Generate new private key
 * 2. Save the file as 'serviceAccountKey.json' in this directory (DO NOT commit to git).
 * 3. Run: npm install firebase-admin
 * 4. Run: node set-admin-claim.js
 */

const admin = require('firebase-admin');
const path = require('path');
const fs = require('fs');

const serviceAccountPath = path.join(__dirname, 'serviceAccountKey.json');

if (fs.existsSync(serviceAccountPath)) {
  const serviceAccount = require(serviceAccountPath);
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });
} else {
  admin.initializeApp();
}

const targetUid = '9FqaKqOScRco4qv4tpNqtqd08Sf1';
const targetEmail = 'mphasamolly@gmail.com';

async function assignAdminClaim() {
  try {
    let user;
    try {
      user = await admin.auth().getUser(targetUid);
    } catch (err) {
      user = await admin.auth().getUserByEmail(targetEmail);
    }

    console.log(`Found target user: ${user.email} (UID: ${user.uid})`);

    const existingClaims = user.customClaims || {};
    const updatedClaims = { ...existingClaims, admin: true };

    await admin.auth().setCustomUserClaims(user.uid, updatedClaims);
    console.log(`SUCCESS: Assigned custom claim { admin: true } to ${user.email} (${user.uid})`);

    const updatedUser = await admin.auth().getUser(user.uid);
    console.log('Verified Custom Claims:', updatedUser.customClaims);
  } catch (error) {
    console.error('ERROR assigning admin claim:', error.message);
  }
}

assignAdminClaim();
