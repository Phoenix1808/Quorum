import { GraphQLClient  } from "graphql-request";

const snap = "https://hub.snapshot.org/graphql";

export const snapClient = new GraphQLClient(snap);