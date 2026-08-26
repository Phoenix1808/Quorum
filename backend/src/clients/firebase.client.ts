import fs from "fs";
import { initializeApp, cert, getApps } from "firebase-admin/app";
import { getMessaging, Messaging } from "firebase-admin/messaging";

// Service account credentials do tarah se aa sakti hain:
//
//   1) Render "Secret Files" -- file /etc/secrets/<naam> par mount hoti hai.
//      Ye preferred hai: multi-line JSON env var me daalne se newline/quote
//      wali dikkat nahi hoti.
//   2) Poora JSON ek env var me (FIREBASE_SERVICE_ACCOUNT).
//
// Dono support karte hain, file pehle dekhte hain.
const SECRET_FILE =
    process.env.FIREBASE_SERVICE_ACCOUNT_FILE ||
    "/etc/secrets/firebase-service-account.json";

function loadCreds(): Record<string, any> | null {
    // 1) mounted secret file
    try {
        if (fs.existsSync(SECRET_FILE)) {
            const txt = fs.readFileSync(SECRET_FILE, "utf8");
            console.log(`Service account file mila: ${SECRET_FILE}`);
            return JSON.parse(txt);
        }
    } catch (err) {
        console.error("Service account file padhne me dikkat:", (err as Error).message);
    }

    // 2) env var me poora JSON
    const raw = process.env.FIREBASE_SERVICE_ACCOUNT;
    if (raw) {
        try {
            console.log("Service account env var se mila");
            return JSON.parse(raw);
        } catch (err) {
            console.error("FIREBASE_SERVICE_ACCOUNT valid JSON nahi hai:", (err as Error).message);
        }
    }

    return null;
}

let messaging: Messaging | null = null;

const creds = loadCreds();
if (!creds) {
    // Push OFF -- par server aur cron normally chalte rahenge.
    console.warn(
        `Service account nahi mila (dekha: ${SECRET_FILE} aur FIREBASE_SERVICE_ACCOUNT) -> push OFF`
    );
} else {
    try {
        // getApps() check isliye ki ts-node-dev respawn par dobara initialize na ho
        if (getApps().length === 0) {
            initializeApp({ credential: cert(creds as any) });
        }
        messaging = getMessaging();
        console.log(`Firebase Admin ready (project: ${creds.project_id}) -> push ON`);
    } catch (err) {
        console.error("Firebase init failed:", (err as Error).message);
    }
}

export { messaging };
