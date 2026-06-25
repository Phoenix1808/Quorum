import {Router} from "express";
import { getDaos } from "../controllers/daos.controller";

const router = Router();
//get /api/daos
router.get("/",getDaos);

export default router;