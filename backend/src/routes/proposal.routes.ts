import { Router } from "express";
import { getProposals } from "../controllers/proposal.controller";

const router = Router();

router.get("/",getProposals);

export default router;