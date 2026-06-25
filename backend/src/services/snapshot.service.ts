import { gql } from "graphql-request";
import { snapClient } from "../clients/snapshot.client";


const propQuery = gql`
  query Proposals($where: ProposalWhere, $first: Int!, $skip: Int!) {
    proposals(
      first: $first
      skip: $skip
      where: $where
      orderBy: "created"
      orderDirection: desc
    ) {
      id title body choices
      start end state
      scores
      scores_total
      space {
        id
        name
      }
    }
  }
`;



interface RawProposal {
    id: string;
    title: string;
    body: string;
    choices: string[];
    start: number;       
    end: number;         
    state: string;      
    scores: number[];    // har choice ke votes [for, against, abstain]
    scores_total: number;
    space: { id: string; name: string };
}

const SpaceQuery = gql`
 query Spaces($ids: [String!]){
 spaces(where: {id_in: $ids}){
 id name about avatar followersCount proposalsCount network
 }
 }
 `;

 interface RawSpace{
    id: string, name:string, avatar:string, about:string, followersCount: number, proposalsCount: number, network:string
 }

 export async function fetchSpaces(ids:string[]) {
    const data = await snapClient.request<{spaces:RawSpace[]}>(
     SpaceQuery, {ids}
    );
    return data.spaces;
 }

export async function fetchProposals(params: {
  spaces: string[];
  state?: string;
  first?: number;
  skip?: number;
}) {
  const { spaces, state, first = 20, skip = 0 } = params;

  
  const where: { space_in: string[]; state?: string } = { space_in: spaces };
  if (state) where.state = state;

  console.log("sending to snapshot:", JSON.stringify({where,first,skip},null,2))
  // query bheji response ka shape batane ke liye 
  const data = await snapClient.request<{ proposals: RawProposal[] }>(
    propQuery,
    { where, first, skip }
  );

//raw snapshot data convert into clean format so that android app gets 
// only the required data and not the whole snapshot data
  return data.proposals.map((p) => ({
    id: p.id,
    title: p.title,
    body: p.body,
    choices: p.choices,
    scores: p.scores,
    scoresTotal: p.scores_total,
    start: p.start,
    end: p.end,
    state: p.state,
    dao: {
      id: p.space.id,
      name: p.space.name,
    },
  }));
}


//single proposal fetch 

const PropQuery = gql`
 query Proposal($id:String!){
 proposal(id: $id){
 id title body
 choices start end
 state scores scores_total
 quorum author created
 snapshot space{id name}}}`;

 export async function fetchProposalById(id:string){
    const data = await snapClient.request<{proposal:any | null}>(
        PropQuery,{id}
    );
    if(!data.proposal)
        return null;

    const p = data.proposal;
    return{
        id: p.id, title: p.title, body: p.body, choices: p.choices, scores: p.scores, scoresTotal :p.scores_total, 
        quorum:p.quorum, author:p.author, created:p.created, 
        // blockchain block number (voting power isi block pe)
        snapshot: p.snapshot,        
         start: p.start,end: p.end, state: p.state,
         dao: {id:p.space.id, name: p.space.name},
    };
 }
