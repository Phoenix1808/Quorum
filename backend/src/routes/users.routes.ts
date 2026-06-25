import { Router } from "express";
import { postUser, postfollow, deleteFollow, getFollows } from "../controllers/user.controller";

const router = Router();

router.post("/", postUser);                              // POST   /api/users
router.post("/:address/follow", postfollow);             // POST   /api/users/:address/follow
router.delete("/:address/follow/:daoId", deleteFollow);  // DELETE /api/users/:address/follow/:daoId
router.get("/:address/follows", getFollows);             // GET    /api/users/:address/follows

export default router;
