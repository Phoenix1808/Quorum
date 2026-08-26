import { Request, Response } from "express";
import { messaging } from "../clients/firebase.client";
import { getToken } from "../services/user.services";

// ⚠️ TEMPORARY — sirf FCM delivery verify karne ke liye.
// Test ho jane par ye file, debug.routes.ts, aur app.ts ka mount HATA DENA.
// Jab tak zinda hai, koi bhi kisi bhi registered address ko push bhej sakta hai.
export async function sendTestPush(req: Request, res: Response) {
    try {
        if (!messaging) {
            res.status(503).json({
                error: "push OFF",
                hint: "service account load nahi hua — Render logs dekho",
            });
            return;
        }

        const address = req.params.address as string;
        const token = await getToken(address);
        if (!token) {
            res.status(404).json({
                error: "is address ka koi fcmToken nahi",
                hint: "app se wallet connect karke register hua tha?",
            });
            return;
        }

        const title = "Quorum test";
        const body = "Ye dikh raha hai = push delivery kaam kar rahi hai.";

        const messageId = await messaging.send({
            token,
            notification: { title, body },
            data: { title, body, proposalId: "test" },
        });

        res.json({ sent: true, messageId });
    } catch (err: any) {
        res.status(500).json({
            error: "send failed",
            detail: err?.message,
            code: err?.errorInfo?.code || err?.code,
        });
    }
}
