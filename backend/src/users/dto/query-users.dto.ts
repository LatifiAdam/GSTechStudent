import { IsEnum, IsOptional } from 'class-validator';
import { Role } from '../../common/enums/roles.enum';

/** GET /users?role=... — Phase 2, section 4.3. */
export class QueryUsersDto {
  @IsOptional()
  @IsEnum(Role)
  role?: Role;
}
