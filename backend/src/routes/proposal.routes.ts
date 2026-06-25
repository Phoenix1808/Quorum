import { Router } from "express";
import { getProposals, getProposalById} from "../controllers/proposal.controller";

const router = Router();

router.get("/",getProposals);
router.get("/:id",getProposalById)

export default router;