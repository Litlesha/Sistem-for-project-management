package org.diploma.fordiplom.entity.DTO.response;


    public class ChatInfoResponse {
        private String title;
        private int membersCount;

        public ChatInfoResponse(String title, int membersCount) {
            this.title = title;
            this.membersCount = membersCount;
        }

        public String getTitle() {
            return title;
        }

        public int getMembersCount() {
            return membersCount;
        }
    }

