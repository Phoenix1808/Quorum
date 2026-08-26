import { Router } from "express";
import { sendTestPush } from "../controllers/debug.controller";

// ⚠️ TEMPORARY — FCM verify hone ke baad hata dena
const router = Router();
router.post("/push/:address", sendTestPush);   // POST /api/debug/push/:address
export default router;
